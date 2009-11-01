/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.fusesource.hawtjni.runtime;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Instructions on how to use the NativeStats tool with a standlaone SWT
 * example:
 * <ol>
 * <li> Compile the native libraries defining the NATIVE_STATS flag.</li>
 * <li> Add the following code around the sections of
 *      interest to dump the native calls done in that section. 
 *      <code><pre>
 *      NativeStats stats = new NativeStats(); ... <code section>
 *      ...
 *      stats.dumpDiff(System.out);
 *      </pre></code>
 * </li>
 * <li> Or add the following code at a given point to dump a snapshot of
 *      the native calls done until that point.
 *      <code><pre>
 *      new NativeStats().dumpSnapshot(System.out);
 *      </pre></code>
 * </li>
 * </ol>
 */
public class NativeStats {

    HashMap<String, ArrayList<NativeFunction>> snapshot;

    final static String[] classes = new String[] { "OS", "ATK", "CDE", "GNOME", "GTK", "XPCOM", "COM", "AGL", "Gdip", "GLX", "Cairo", "WGL" };

    public static class NativeFunction implements Comparable<NativeFunction> {
        
        final String name;
        int callCount;

        public NativeFunction(String name, int callCount) {
            this.name = name;
            this.callCount = callCount;
        }

        void subtract(NativeFunction func) {
            this.callCount -= func.callCount;
        }

        public int getCallCount() {
            return callCount;
        }

        public String getName() {
            return name;
        }

        public int compareTo(NativeFunction func) {
            return func.callCount - callCount;
        }
    }

    public NativeStats() {
        snapshot = snapshot();
    }

    public HashMap<String, ArrayList<NativeFunction>> diff() {
        HashMap<String, ArrayList<NativeFunction>> newSnapshot = snapshot();
        for (Entry<String, ArrayList<NativeFunction>> entry : newSnapshot.entrySet()) {
            String className = entry.getKey();
            ArrayList<NativeFunction> newFuncs = entry.getValue();
            ArrayList<NativeFunction> funcs = snapshot.get(className);
            if (funcs != null) {
                for (int i = 0; i < newFuncs.size(); i++) {
                    newFuncs.get(i).subtract(funcs.get(i));
                }
            }
        }
        return newSnapshot;
    }

    public void dumpDiff(PrintStream ps) {
        dump(diff(), ps);
    }

    public void dumpSnapshot(PrintStream ps) {
        dump(snapshot(), ps);
    }

    public void dumpSnapshot(String className, PrintStream ps) {
        HashMap<String, ArrayList<NativeFunction>> snapshot = new HashMap<String, ArrayList<NativeFunction>>();
        snapshot(className, snapshot);
        dump(className, snapshot.get(className), ps);
    }

    public void dump(HashMap<String, ArrayList<NativeFunction>> snapshot, PrintStream ps) {
        for (Entry<String, ArrayList<NativeFunction>> entry : snapshot.entrySet()) {
            dump(entry.getKey(), entry.getValue(), ps);
        }
    }

    void dump(String className, ArrayList<NativeFunction> funcs, PrintStream ps) {
        if (funcs == null)
            return;
        Collections.sort(funcs);
        int total = 0;
        for (NativeFunction func : funcs) {
            total += func.getCallCount();
        }
        ps.print(className);
        ps.print("=");
        ps.print(total);
        ps.println();
        for (NativeFunction func : funcs) {
            if (func.getCallCount() > 0) {
                ps.print("\t");
                ps.print(func.getName());
                ps.print("=");
                ps.print(func.getCallCount());
                ps.println();
            }
        }
    }

    public void reset() {
        snapshot = snapshot();
    }

    public HashMap<String, ArrayList<NativeFunction>> snapshot() {
        HashMap<String, ArrayList<NativeFunction>> snapshot = new HashMap<String, ArrayList<NativeFunction>>();
        for (int i = 0; i < classes.length; i++) {
            String className = classes[i];
            snapshot(className, snapshot);
        }
        return snapshot;
    }

    public HashMap<String, ArrayList<NativeFunction>> snapshot(String className, HashMap<String, ArrayList<NativeFunction>> snapshot) {
        try {
            Class<?> clazz = getClass();
            Method functionCount = clazz.getMethod(className + "_GetFunctionCount", new Class[0]);
            Method functionCallCount = clazz.getMethod(className + "_GetFunctionCallCount", new Class[] { int.class });
            Method functionName = clazz.getMethod(className + "_GetFunctionName", new Class[] { int.class });
            
            int count = ((Integer) functionCount.invoke(clazz, new Object[]{})).intValue();
            ArrayList<NativeFunction> funcs = new ArrayList<NativeFunction>(count);
            for (int i = 0; i < count; i++) {
                Object[] args = new Object[]{i};
                int callCount = ((Integer) functionCallCount.invoke(clazz, args)).intValue();
                String name = (String) functionName.invoke(clazz, args);
                funcs.add( new NativeFunction(name, callCount) );
            }
            
            snapshot.put(className, funcs);
            
        } catch (Throwable e) {
            // e.printStackTrace(System.out);
        }
        return snapshot;
    }
}
