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
package org.fusesource.hawtjni.generator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import org.fusesource.hawtjni.generator.model.JNIClass;
import org.fusesource.hawtjni.generator.model.JNIField;
import org.fusesource.hawtjni.generator.model.JNIItem;
import org.fusesource.hawtjni.generator.model.JNIMethod;
import org.fusesource.hawtjni.generator.model.JNIType;

public abstract class JNIGenerator implements Flags {
    
    static final String delimiter = System.getProperty("line.separator");
    static final String JNI64 = "JNI64";

    ArrayList<JNIClass> classes;
    String copyright = "";
    boolean isCPP;
    PrintStream output = System.out;
    ProgressMonitor progress;
    private String outputName;
    
    static String fixDelimiter(String str) {
        if (delimiter.equals("\n")) {
            return str;
        }
        return str.replaceAll("\n", delimiter);
    }

    static String getFunctionName(JNIMethod method) {
        return getFunctionName(method, method.getParameterTypes());
    }

    static String getFunctionName(JNIMethod method, JNIType[] paramTypes) {
        if ((method.getModifiers() & Modifier.NATIVE) == 0)
            return method.getName();
        String function = toC(method.getName());
        if (!method.isNativeUnique()) {
            StringBuffer buffer = new StringBuffer();
            buffer.append(function);
            buffer.append("__");
            for (int i = 0; i < paramTypes.length; i++) {
                JNIType paramType = paramTypes[i];
                buffer.append(toC(paramType.getTypeSignature(false)));
            }
            return buffer.toString();
        }
        return function;
    }

    static String loadFile(String file) {
        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            StringBuffer str = new StringBuffer();
            char[] buffer = new char[1024];
            int read;
            while ((read = br.read(buffer)) != -1) {
                str.append(buffer, 0, read);
            }
            fr.close();
            return str.toString();
        } catch (IOException e) {
            throw new RuntimeException("File not found:" + file, e);
        }
    }

    public static void sort(JNIMethod[] methods) {
        Arrays.sort(methods, new Comparator<JNIMethod>() {
            public int compare(JNIMethod mth1, JNIMethod mth2) {
                int result = mth1.getName().compareTo(mth2.getName());
                return result != 0 ? result : getFunctionName(mth1).compareTo(getFunctionName(mth2));
            }
        });
    }

    static void sort(JNIField[] fields) {
        Arrays.sort(fields, new Comparator<JNIField>() {
            public int compare(JNIField a, JNIField b) {
                return a.getName().compareTo(b.getName());
            }
        });
    }

    static void sort(ArrayList<JNIClass> classes) {
        Collections.sort(classes, new Comparator<JNIClass>() {
            public int compare(JNIClass a, JNIClass b) {
                return a.getName().compareTo(b.getName());
            }
        });
    }

    static String toC(String str) {
        int length = str.length();
        StringBuffer buffer = new StringBuffer(length * 2);
        for (int i = 0; i < length; i++) {
            char c = str.charAt(i);
            switch (c) {
            case '_':
                buffer.append("_1");
                break;
            case ';':
                buffer.append("_2");
                break;
            case '[':
                buffer.append("_3");
                break;
            case '.':
                buffer.append("_");
                break;
            case '/':
                buffer.append("_");
                break;
            default:
                buffer.append(c);
            }
        }
        return buffer.toString();
    }

    public abstract void generate(JNIClass clazz);

    public void generateCopyright() {
    }

    public void generateIncludes() {
    }

    public void generate() {
        if (classes == null)
            return;
        generateCopyright();
        generateIncludes();
        sort(classes);
        for (JNIClass clazz : classes) {
            if (clazz.getFlag(FLAG_CPP)) {
                isCPP = true;
                break;
            }
        }
        for (JNIClass clazz : classes) {
            if (getGenerate(clazz))
                generate(clazz);
            if (progress != null)
                progress.step();
        }
        output.flush();
    }

    public boolean getCPP() {
        return isCPP;
    }

    public String getDelimiter() {
        return delimiter;
    }

    protected boolean getGenerate(JNIItem item) {
        return item.getGenerate();
    }

    public PrintStream getOutput() {
        return output;
    }

    public String getOutputName() {
        return outputName;
    }

    public void setOutputName(String outputName) {
        this.outputName = outputName;
    }

    public ProgressMonitor getProgressMonitor() {
        return progress;
    }

    public void output(String str) {
        output.print(str);
    }

    public void outputln() {
        output(getDelimiter());
    }

    public void outputln(String str) {
        output(str);
        output(getDelimiter());
    }

    public void setClasses(ArrayList<JNIClass> classes) {
        this.classes = classes;
    }

    public void setOutput(PrintStream output) {
        this.output = output;
    }

    public void setProgressMonitor(ProgressMonitor progress) {
        this.progress = progress;
    }
    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

}
