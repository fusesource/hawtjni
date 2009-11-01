/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.fusesource.hawtjni.generator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class JNIGeneratorApp {

    MetaData metaData = new MetaData();

    JNIClass mainClass;
    JNIClass[] classes;
    ProgressMonitor progress;
    String mainClassName;
    String nativeOutputDir;
    String classpath;

    public JNIGeneratorApp() {
    }

    public String getClasspath() {
        return classpath;
    }

    public JNIClass getMainClass() {
        return mainClass;
    }

    public String getMainClassName() {
        return mainClassName;
    }

    public MetaData getMetaData() {
        return metaData;
    }

    String getMetaDataDir() {
        return "./JNI Generation/org/eclipse/swt/tools/internal/";
    }

    public String getNativeOutputDir() {
        return nativeOutputDir;
    }

    void generateSTATS_C(JNIClass[] classes) {
        try {
            StatsGenerator gen = new StatsGenerator(false);
            gen.setMainClass(mainClass);
            gen.setClasses(classes);
            gen.setMetaData(metaData);
            gen.setProgressMonitor(progress);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            gen.setOutput(new PrintStream(out));
            gen.generate();
            if (out.size() > 0)
                JNIGenerator.output(out.toByteArray(), nativeOutputDir + gen.getFileName());
        } catch (Exception e) {
            System.out.println("Problem");
            e.printStackTrace(System.out);
        }
    }

    void generateSTATS_H(JNIClass[] classes) {
        try {
            StatsGenerator gen = new StatsGenerator(true);
            gen.setMainClass(mainClass);
            gen.setClasses(classes);
            gen.setMetaData(metaData);
            gen.setProgressMonitor(progress);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            gen.setOutput(new PrintStream(out));
            gen.generate();
            if (out.size() > 0)
                JNIGenerator.output(out.toByteArray(), nativeOutputDir + gen.getFileName());
        } catch (Exception e) {
            System.out.println("Problem");
            e.printStackTrace(System.out);
        }
    }

    void generateSTRUCTS_H(JNIClass[] classes) {
        try {
            StructsGenerator gen = new StructsGenerator(true);
            gen.setMainClass(mainClass);
            gen.setClasses(classes);
            gen.setMetaData(metaData);
            gen.setProgressMonitor(progress);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            gen.setOutput(new PrintStream(out));
            gen.generate();
            if (out.size() > 0)
                JNIGenerator.output(out.toByteArray(), nativeOutputDir + gen.getFileName());
        } catch (Exception e) {
            System.out.println("Problem");
            e.printStackTrace(System.out);
        }

    }

    void generateSTRUCTS_C(JNIClass[] classes) {
        try {
            StructsGenerator gen = new StructsGenerator(false);
            gen.setMainClass(mainClass);
            gen.setClasses(classes);
            gen.setMetaData(metaData);
            gen.setProgressMonitor(progress);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            gen.setOutput(new PrintStream(out));
            gen.generate();
            if (out.size() > 0)
                JNIGenerator.output(out.toByteArray(), nativeOutputDir + gen.getFileName());
        } catch (Exception e) {
            System.out.println("Problem");
            e.printStackTrace(System.out);
        }

    }

    void generateSWT_C(JNIClass[] classes) {
        try {
            NativesGenerator gen = new NativesGenerator();
            gen.setMainClass(mainClass);
            gen.setClasses(classes);
            gen.setMetaData(metaData);
            gen.setProgressMonitor(progress);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            gen.setOutput(new PrintStream(out));
            gen.generate();
            if (out.size() > 0)
                JNIGenerator.output(out.toByteArray(), nativeOutputDir + gen.getFileName());
        } catch (Exception e) {
            System.out.println("Problem");
            e.printStackTrace(System.out);
        }
    }

    public void generate() {
        generate(null);
    }

    public void generate(ProgressMonitor progress) {
        if (mainClass == null)
            return;
        if (progress != null)
            progress.setMessage("Initializing...");
        JNIClass[] classes = getClasses();
        JNIClass[] natives = getNativesClasses(classes);
        JNIClass[] structs = getStructureClasses(classes);
        this.progress = progress;
        if (progress != null) {
            int nativeCount = 0;
            for (int i = 0; i < natives.length; i++) {
                JNIClass clazz = natives[i];
                JNIMethod[] methods = clazz.getDeclaredMethods();
                for (int j = 0; j < methods.length; j++) {
                    JNIMethod method = methods[j];
                    if ((method.getModifiers() & Modifier.NATIVE) == 0)
                        continue;
                    nativeCount++;
                }
            }
            int total = nativeCount * 4;
            total += classes.length;
            total += natives.length * (3);
            total += structs.length * 2;
            progress.setTotal(total);
            progress.setMessage("Generating structs.h ...");
        }
        generateSTRUCTS_H(structs);
        if (progress != null)
            progress.setMessage("Generating structs.c ...");
        generateSTRUCTS_C(structs);
        if (progress != null)
            progress.setMessage("Generating natives ...");
        generateSWT_C(natives);
        if (progress != null)
            progress.setMessage("Generating stats.h ...");
        generateSTATS_H(natives);
        if (progress != null)
            progress.setMessage("Generating stats.c ...");
        generateSTATS_C(natives);
        if (progress != null)
            progress.setMessage("Done.");
        this.progress = null;
    }

    String getPackageName(String className) {
        int dot = mainClassName.lastIndexOf('.');
        if (dot == -1)
            return "";
        return mainClassName.substring(0, dot);
    }

    String[] getClassNames(String mainClassName) {
        String pkgName = getPackageName(mainClassName);
        String classpath = getClasspath();
        if (classpath == null)
            classpath = System.getProperty("java.class.path");
        String pkgPath = pkgName.replace('.', File.separatorChar);
        String pkgZipPath = pkgName.replace('.', '/');
        ArrayList<String> classes = new ArrayList<String>();
        int start = 0;
        int index = 0;
        while (index < classpath.length()) {
            index = classpath.indexOf(File.pathSeparatorChar, start);
            if (index == -1)
                index = classpath.length();
            String path = classpath.substring(start, index);
            if (path.toLowerCase().endsWith(".jar")) {
                ZipFile zipFile = null;
                try {
                    zipFile = new ZipFile(path);
                    Enumeration<? extends ZipEntry> entries = zipFile.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = (ZipEntry) entries.nextElement();
                        String name = entry.getName();
                        if (name.startsWith(pkgZipPath) && name.indexOf('/', pkgZipPath.length() + 1) == -1 && name.endsWith(".class")) {
                            String className = name.substring(pkgZipPath.length() + 1, name.length() - 6);
                            className.replace('/', '.');
                            classes.add(className);
                        }
                    }
                } catch (IOException e) {
                } finally {
                    try {
                        if (zipFile != null)
                            zipFile.close();
                    } catch (IOException ex) {
                    }
                }
            } else {
                File file = new File(path + File.separator + pkgPath);
                if (file.exists()) {
                    String[] entries = file.list();
                    for (int i = 0; i < entries.length; i++) {
                        String entry = entries[i];
                        File f = new File(file, entry);
                        if (!f.isDirectory()) {
                            if (f.getAbsolutePath().endsWith(".class")) {
                                String className = entry.substring(0, entry.length() - 6);
                                classes.add(className);
                            }
                        }
                    }
                }
            }
            start = index + 1;
        }
        return classes.toArray(new String[classes.size()]);
    }

    public JNIClass[] getClasses() {
        
        if (classes != null)
            return classes;
        if (mainClassName == null)
            return new JNIClass[0];
        
        String[] classNames = getClassNames(mainClassName);
        Arrays.sort(classNames);
        String packageName = getPackageName(mainClassName);
        JNIClass[] classes = new JNIClass[classNames.length];
        for (int i = 0; i < classNames.length; i++) {
            String className = classNames[i];
            try {
                String qualifiedName = packageName + "." + className;
                if (qualifiedName.equals(mainClassName)) {
                    classes[i] = mainClass;
                } else {
                    classes[i] = new ReflectClass(Class.forName(qualifiedName, false, getClass().getClassLoader()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return classes;
    }

    public JNIClass[] getNativesClasses(JNIClass[] classes) {
        if (mainClass == null)
            return new JNIClass[0];
        ArrayList<JNIClass> result = new ArrayList<JNIClass>();
        for (int i = 0; i < classes.length; i++) {
            JNIClass clazz = classes[i];
            JNIMethod[] methods = clazz.getDeclaredMethods();
            for (int j = 0; j < methods.length; j++) {
                JNIMethod method = methods[j];
                int mods = method.getModifiers();
                if ((mods & Modifier.NATIVE) != 0) {
                    result.add(clazz);
                    break;
                }
            }
        }
        return result.toArray(new JNIClass[result.size()]);
    }

    public JNIClass[] getStructureClasses(JNIClass[] classes) {
        if (mainClass == null)
            return new JNIClass[0];
        ArrayList<JNIClass> result = new ArrayList<JNIClass>();
        outer: for (int i = 0; i < classes.length; i++) {
            JNIClass clazz = classes[i];
            JNIMethod[] methods = clazz.getDeclaredMethods();
            for (int j = 0; j < methods.length; j++) {
                JNIMethod method = methods[j];
                int mods = method.getModifiers();
                if ((mods & Modifier.NATIVE) != 0)
                    continue outer;
            }
            JNIField[] fields = clazz.getDeclaredFields();
            boolean hasPublicFields = false;
            for (int j = 0; j < fields.length; j++) {
                JNIField field = fields[j];
                int mods = field.getModifiers();
                if ((mods & Modifier.PUBLIC) != 0 && (mods & Modifier.STATIC) == 0) {
                    hasPublicFields = true;
                    break;
                }
            }
            if (!hasPublicFields)
                continue;
            result.add(clazz);
        }
        return result.toArray(new JNIClass[result.size()]);
    }

    public void setClasspath(String classpath) {
        this.classpath = classpath;
    }

    public void setMainClass(JNIClass mainClass) {
        this.mainClass = mainClass;
    }

    public void setMetaData(MetaData data) {
        this.metaData = data;
    }

    public void setClasses(JNIClass[] classes) {
        this.classes = classes;
    }

    public void setMainClassName(String mainClassName) {
        if (mainClassName != null) {
            try {
                mainClass = new ReflectClass(Class.forName(mainClassName, false, getClass().getClassLoader()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setMainClassName(String str, String outputDir) {
        mainClassName = str;
        setNativeOutputDir(outputDir);
        try {
            mainClass = new ReflectClass(Class.forName(mainClassName, false, getClass().getClassLoader()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setNativeOutputDir(String str) {
        if (str != null) {
            if (!str.endsWith("\\") && !str.endsWith("/")) {
                str += File.separator;
            }
        }
        nativeOutputDir = str.replace('\\', '/');
    }

    public static void main(String[] args) {
        JNIGeneratorApp gen = new JNIGeneratorApp();
        if (args.length > 0) {
            gen.setMainClassName(args[0]);
            if (args.length > 1)
                gen.setNativeOutputDir(args[1]);
            if (args.length > 2)
                gen.setClasspath(args[2]);
        } else {
            System.out.println("Invalid usage..");
        }
        gen.generate();
    }

}
