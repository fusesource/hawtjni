/*******************************************************************************
 * Copyright (C) 2009-2011 FuseSource Corp.
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.fusesource.hawtjni.runtime;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Instructions on how to use the NativeStats tool with a standalone SWT
 * example:
 * <ol>
 * <li> Compile the native libraries defining the NATIVE_STATS flag.</li>
 * <li> Add the following code around the sections of
 *      interest to dump the native calls done in that section. 
 *      <code><pre>
 *      StatsInterface si = MyFooStatsInterface.INSTANCE;
 *      NativeStats stats = new NativeStats(si); 
 *      ... // your code
 *      stats.diff().dump(System.out);
 *      </pre></code>
 * </li>
 * <li> Or add the following code at a given point to dump a snapshot of
 *      the native calls done until that point.
 *      <code><pre>
 *      stats.snapshot().dump(System.out);
 *      </pre></code>
 * </li>
 * </ol>
 * 
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class NativeStats {

    public interface StatsInterface {
        String getNativeClass();
        int functionCount();
        String functionName(int ordinal);
        int functionCounter(int ordinal);
    }
    
    public static class NativeFunction implements Comparable<NativeFunction> {
        private final int ordinal;
        private final String name;
        private int counter;

        public NativeFunction(int ordinal, String name, int callCount) {
            this.ordinal = ordinal;
            this.name = name;
            this.counter = callCount;
        }
        void subtract(NativeFunction func) {
            this.counter -= func.counter;
        }
        
        public int getCounter() {
            return counter;
        }
        public void setCounter(int counter) {
            this.counter = counter;
        }
        
        public String getName() {
            return name;
        }

        public int getOrdinal() {
            return ordinal;
        }
        
        public int compareTo(NativeFunction func) {
            return func.counter - counter;
        }
        
        public void reset() {
            counter=0;
        }
        
        public NativeFunction copy() {
            return new NativeFunction(ordinal, name, counter);
        }
    }

    private final HashMap<StatsInterface, ArrayList<NativeFunction>> snapshot;
    
    public NativeStats(StatsInterface... classes) {
        this(Arrays.asList(classes)); 
    }

    public NativeStats(Collection<StatsInterface> classes) {
        this(snapshot(classes)); 
    }
    
    private NativeStats(HashMap<StatsInterface, ArrayList<NativeFunction>> snapshot) {
        this.snapshot = snapshot;
    }

    public void reset() {
        for (ArrayList<NativeFunction> functions : snapshot.values()) {
            for (NativeFunction function : functions) {
                function.reset();
            }
        }
    }
    
    public void update() {
        for (Entry<StatsInterface, ArrayList<NativeFunction>> entry : snapshot.entrySet()) {
            StatsInterface si = entry.getKey();
            for (NativeFunction function : entry.getValue()) {
                function.setCounter( si.functionCounter(function.getOrdinal()) );
            }
        }
    }
    
    public NativeStats snapshot() {
        NativeStats copy = copy();
        copy.update();
        return copy;
    }

    public NativeStats copy() {
        HashMap<StatsInterface, ArrayList<NativeFunction>> rc = new HashMap<StatsInterface, ArrayList<NativeFunction>>(snapshot.size()*2);
        for (Entry<StatsInterface, ArrayList<NativeFunction>> entry : snapshot.entrySet()) {
            ArrayList<NativeFunction> list = new ArrayList<NativeFunction>(entry.getValue().size());
            for (NativeFunction function : entry.getValue()) {
                list.add(function.copy());
            }
            rc.put(entry.getKey(), list);
        }
        return new NativeStats(rc);
    }
    
    public NativeStats diff() {
        HashMap<StatsInterface, ArrayList<NativeFunction>> rc = new HashMap<StatsInterface, ArrayList<NativeFunction>>(snapshot.size()*2);
        for (Entry<StatsInterface, ArrayList<NativeFunction>> entry : snapshot.entrySet()) {
            StatsInterface si = entry.getKey();
            ArrayList<NativeFunction> list = new ArrayList<NativeFunction>(entry.getValue().size());
            for (NativeFunction original : entry.getValue()) {
                NativeFunction copy = original.copy();
                copy.setCounter( si.functionCounter(copy.getOrdinal()) );
                copy.subtract(original);
                list.add(copy);
            }
            rc.put(si, list);
        }
        return new NativeStats(rc);
    }

    /**
     * Dumps the stats to the print stream in a JSON format.
     * @param ps
     */
    public void dump(PrintStream ps) {
        boolean firstSI=true;
        for (Entry<StatsInterface, ArrayList<NativeFunction>> entry : snapshot.entrySet()) {
            StatsInterface si = entry.getKey();
            ArrayList<NativeFunction> funcs = entry.getValue();

            int total = 0;
            for (NativeFunction func : funcs) {
                total += func.getCounter();
            }
            
            if( !firstSI ) {
                ps.print(", ");
            }
            firstSI=false;
            ps.print("[");
            if( total>0 ) {
                ps.println("{ ");
                ps.println("  \"class\": \""+si.getNativeClass()+"\",");
                ps.println("  \"total\": "+total+", ");
                  ps.print("  \"functions\": {");
                boolean firstFunc=true;
                for (NativeFunction func : funcs) {
                    if (func.getCounter() > 0) {
                        if( !firstFunc ) {
                            ps.print(",");
                        }
                        firstFunc=false;
                        ps.println();
                        ps.print("    \""+func.getName()+"\": "+func.getCounter());
                    }
                }
                ps.println();
                ps.println("  }");
                ps.print("}");
            }            
            ps.print("]");
        }
    }

    static private HashMap<StatsInterface, ArrayList<NativeFunction>> snapshot(Collection<StatsInterface> classes) {
         HashMap<StatsInterface, ArrayList<NativeFunction>> rc = new HashMap<StatsInterface, ArrayList<NativeFunction>>();
        for (StatsInterface sc : classes) {
            int count = sc.functionCount();
            ArrayList<NativeFunction> functions = new ArrayList<NativeFunction>(count);
            for (int i = 0; i < count; i++) {
                String name = (String) sc.functionName(i);
                functions.add(new NativeFunction(i, name, 0));
            }
            Collections.sort(functions);
            rc.put(sc, functions);
        }
        return rc;
    }
    

}
