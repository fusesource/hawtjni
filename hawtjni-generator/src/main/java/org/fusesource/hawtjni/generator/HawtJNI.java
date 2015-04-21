/*******************************************************************************
 * Copyright (C) 2009-2011 FuseSource Corp.
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.fusesource.hawtjni.generator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.xbean.finder.ClassFinder;
import org.apache.xbean.finder.UrlSet;
import org.fusesource.hawtjni.generator.model.JNIClass;
import org.fusesource.hawtjni.generator.model.ReflectClass;
import org.fusesource.hawtjni.generator.util.FileSupport;
import org.fusesource.hawtjni.runtime.ClassFlag;
import org.fusesource.hawtjni.runtime.JniClass;

import static org.fusesource.hawtjni.generator.util.OptionBuilder.*;

/**
 * 
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class HawtJNI {
    public static final String END_YEAR_TAG = "%END_YEAR%";

    private ProgressMonitor progress;
    private File nativeOutput = new File(".");
//    private File javaOutputDir = new File(".");
    private List<String> classpaths = new ArrayList<String>();
    private List<String> packages = new ArrayList<String>();
    private String name = "hawtjni_native";
    private String copyright = "";
    private boolean callbacks = true;
    
    ///////////////////////////////////////////////////////////////////
    // Command line entry point
    ///////////////////////////////////////////////////////////////////
    public static void main(String[] args) {
        String jv = System.getProperty("java.version").substring(0, 3);
        if (jv.compareTo("1.5") < 0) {
            System.err.println("This application requires jdk 1.5 or higher to run, the current java version is " + System.getProperty("java.version"));
            System.exit(-1);
            return;
        }

        HawtJNI app = new HawtJNI();
        System.exit(app.execute(args));
    }
    
    ///////////////////////////////////////////////////////////////////
    // Entry point for an embedded users who want to call us with
    // via command line arguments.
    ///////////////////////////////////////////////////////////////////
    public int execute(String[] args) {
        CommandLine cli = null;
        try {
            cli = new PosixParser().parse(createOptions(), args, true);
        } catch (ParseException e) {
            System.err.println( "Unable to parse command line options: " + e.getMessage() );
            displayHelp();
            return 1;
        }

        if( cli.hasOption("h") ) {
            displayHelp();
            return 0;
        }
        
        if( cli.hasOption("v") ) {
            progress = new ProgressMonitor() {
                public void step() {
                }
                public void setTotal(int total) {
                }
                public void setMessage(String message) {
                    System.out.println(message);
                }
            };
        }
        
        name = cli.getOptionValue("n", "hawtjni_native");
        nativeOutput = new File(cli.getOptionValue("o", "."));
//        javaOutputDir = new File(cli.getOptionValue("j", "."));
        String[] values = cli.getOptionValues("p");
        if( values!=null ) {
            packages = Arrays.asList(values);
        }
        
        values = cli.getArgs();
        if( values!=null ) {
            classpaths = Arrays.asList(values);
        }

        try {
            if( classpaths.isEmpty() ) {
                throw new UsageException("No classpath supplied.");
            }
            generate();
        } catch (UsageException e) {
            System.err.println("Invalid usage: "+e.getMessage());
            displayHelp();
            return 1;
        } catch (Throwable e) {
            System.out.flush();
            System.err.println("Unexpected failure:");
            e.printStackTrace();
            Set<Throwable> exceptions = new HashSet<Throwable>();
            exceptions.add(e);
            for (int i = 0; i < 10; i++) {
                e = e.getCause();
                if (e != null && exceptions.add(e)) {
                    System.err.println("Reason: " + e);
                    e.printStackTrace();
                } else {
                    break;
                }
            }
            return 2;
        }
        return 0;
    }

    
    ///////////////////////////////////////////////////////////////////
    // Entry point for an embedded users who want use us like a pojo
    ///////////////////////////////////////////////////////////////////
    public ProgressMonitor getProgress() {
        return progress;
    }

    public void setProgress(ProgressMonitor progress) {
        this.progress = progress;
    }

    public File getNativeOutput() {
        return nativeOutput;
    }

    public void setNativeOutput(File nativeOutput) {
        this.nativeOutput = nativeOutput;
    }

    public List<String> getClasspaths() {
        return classpaths;
    }

    public void setClasspaths(List<String> classpaths) {
        this.classpaths = classpaths;
    }

    public List<String> getPackages() {
        return packages;
    }

    public void setPackages(List<String> packages) {
        this.packages = packages;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }
    
    public boolean isCallbacks() {
        return callbacks;
    }

    public void setCallbacks(boolean enableCallbacks) {
        this.callbacks = enableCallbacks;
    }

    public void generate() throws UsageException, IOException {
        progress("Analyzing classes...");
        
        ArrayList<JNIClass> natives = new ArrayList<JNIClass>();
        ArrayList<JNIClass> structs = new ArrayList<JNIClass>();
        findClasses(natives, structs);
        
        if( natives.isEmpty() && structs.isEmpty() ) {
            throw new RuntimeException("No @JniClass or @JniStruct annotated classes found.");
        }

        
        if (progress != null) {
            int nativeCount = 0;
            for (JNIClass clazz : natives) {
                nativeCount += clazz.getNativeMethods().size();
            }
            int total = nativeCount * 4;
            total += natives.size() * (3);
            total += structs.size() * 2;
            progress.setTotal(total);
        }
        
        File file;
        nativeOutput.mkdirs();
        
        progress("Generating...");
        file = nativeFile(".c");
        generate(new NativesGenerator(), natives, file);

        file = nativeFile("_stats.h");
        generate(new StatsGenerator(true), natives, file);
        
        file = nativeFile("_stats.c");
        generate(new StatsGenerator(false), natives, file);

        file = nativeFile("_structs.h");
        generate(new StructsGenerator(true), structs, file);
        
        file = nativeFile("_structs.c");
        generate(new StructsGenerator(false), structs, file);
        
        file = new File(nativeOutput, "hawtjni.h");
        generateFromResource("hawtjni.h", file);
        
        file = new File(nativeOutput, "hawtjni.c");
        generateFromResource("hawtjni.c", file);

        file = new File(nativeOutput, "hawtjni-callback.c");
        if( callbacks ) {
            generateFromResource("hawtjni-callback.c", file);
        } else {
            file.delete();
        }
        
        file = new File(nativeOutput, "windows");
        file.mkdirs();
        file = new File(file, "stdint.h");
        generateFromResource("windows/stdint.h", file);
        
        progress("Done.");
    }

    ///////////////////////////////////////////////////////////////////
    // Helper methods
    ///////////////////////////////////////////////////////////////////

    private void findClasses(ArrayList<JNIClass> jni, ArrayList<JNIClass> structs) throws UsageException {
        
        ArrayList<URL> urls = new ArrayList<URL>();
        for (String classpath : classpaths) {
            String[] fileNames = classpath.replace(';', ':').split(":");
            for (String fileName : fileNames) {
                try {
                    File file = new File(fileName);
                    if( file.isDirectory() ) {
                        urls.add(new URL(url(file)+"/"));
                    } else {
                        urls.add(new URL(url(file)));
                    }
                } catch (Exception e) {
                    throw new UsageException("Invalid class path.  Not a valid file: "+fileName);
                }
            }
        }
        LinkedHashSet<Class<?>> jniClasses = new LinkedHashSet<Class<?>>(); 
        try {
            URLClassLoader classLoader = new URLClassLoader(array(URL.class, urls), JniClass.class.getClassLoader());
            UrlSet urlSet = new UrlSet(classLoader);
            urlSet = urlSet.excludeJavaHome();
            ClassFinder finder = new ClassFinder(classLoader, urlSet.getUrls());
            collectMatchingClasses(finder, JniClass.class, jniClasses);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        for (Class<?> clazz : jniClasses) {
            ReflectClass rc = new ReflectClass(clazz);
            if( rc.getFlag(ClassFlag.STRUCT) ) {
                structs.add(rc);
            }
            if( !rc.getNativeMethods().isEmpty() ) {
                jni.add(rc);
            }
        }
    }
    
    
    static private Options createOptions() {
        Options options = new Options();
        options.addOption("h", "help",    false, "Display help information");
        options.addOption("v", "verbose", false, "Verbose generation");

        options.addOption("o", "offline", false, "Work offline");
        options.addOption(ob()
                .id("n")
                .name("name")
                .arg("value")
                .description("The base name of the library, used to determine generated file names.  Defaults to 'hawtjni_native'.").op());

        options.addOption(ob()
                .id("o")
                .name("native-output")
                .arg("dir")
                .description("Directory where generated native source code will be stored.  Defaults to the current directory.").op());

//        options.addOption(ob()
//                .id("j")
//                .name("java-output")
//                .arg("dir")
//                .description("Directory where generated native source code will be stored.  Defaults to the current directory.").op());

        options.addOption(ob()
                .id("p")
                .name("package")
                .arg("package")
                .description("Restrict looking for JNI classes to the specified package.").op());
        
        return options;
    }
    

    private void displayHelp() {
        System.err.flush();
        String app = System.getProperty("hawtjni.application");
        if( app == null ) {
            try {
                URL location = getClass().getProtectionDomain().getCodeSource().getLocation();
                String[] split = location.toString().split("/");
                if( split[split.length-1].endsWith(".jar") ) {
                    app = split[split.length-1];
                }
            } catch (Throwable e) {
            }
            if( app == null ) {
                app = getClass().getSimpleName();
            }
        }

        // The commented out line is 80 chars long.  We have it here as a visual reference
//      p("                                                                                ");
        p();
        p("Usage: "+ app +" [options] <classpath>");
        p();
        p("Description:");
        p();
        pw("  "+app+" is a code generator that produces the JNI code needed to implement java native methods.", 2);
        p();

        p("Options:");
        p();
        PrintWriter out = new PrintWriter(System.out);
        HelpFormatter formatter = new HelpFormatter();
        formatter.printOptions(out, 78, createOptions(), 2, 2);
        out.flush();
        p();
        p("Examples:");
        p();
        pw("  "+app+" -o build foo.jar bar.jar ", 2);
        pw("  "+app+" -o build foo.jar:bar.jar ", 2);
        pw("  "+app+" -o build -p org.mypackage foo.jar;bar.jar ", 2);
        p();
    }

    private void p() {
        System.out.println();
    }
    private void p(String s) {
        System.out.println(s);
    }
    private void pw(String message, int indent) {
        PrintWriter out = new PrintWriter(System.out);
        HelpFormatter formatter = new HelpFormatter();
        formatter.printWrapped(out, 78, indent, message);
        out.flush();
    }
    
    @SuppressWarnings("unchecked")
    private void collectMatchingClasses(ClassFinder finder, Class annotation, LinkedHashSet<Class<?>> collector) {
        List<Class<?>> annotated = finder.findAnnotatedClasses(annotation);
        for (Class<?> clazz : annotated) {
            if( packages.isEmpty() ) {
                collector.add(clazz);
            } else {
                if( packages.contains(clazz.getPackage().getName()) ) {
                    collector.add(clazz);
                }
            }
        }
    }
    
    private void progress(String message) {
        if (progress != null) {
            progress.setMessage(message);
        }
    }

    private void generate(JNIGenerator gen, ArrayList<JNIClass> classes, File target) throws IOException {
        gen.setOutputName(name);
        gen.setClasses(classes);
        gen.setCopyright(getCopyright());
        gen.setProgressMonitor(progress);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        gen.setOutput(new PrintStream(out));
        gen.generate();
        if (out.size() > 0) {
            if( target.getName().endsWith(".c") && gen.isCPP ) {
                target = new File(target.getParentFile(), target.getName()+"pp");
            }
            if( FileSupport.write(out.toByteArray(), target) ) {
                progress("Wrote: "+target);
            }
        }
    }

    private void generateFromResource(String resource, File target) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream is = getClass().getClassLoader().getResourceAsStream(resource);
        FileSupport.copy(is, out);
        String content = new String(out.toByteArray(), "UTF-8");
        String[] parts = content.split(Pattern.quote("/* == HEADER-SNIP-LOCATION == */"));
        if( parts.length==2 ) {
            content = parts[1];
        }
        out.reset();
        PrintStream ps = new PrintStream(out);
        ps.print(JNIGenerator.fixDelimiter(getCopyright()));
        ps.print(JNIGenerator.fixDelimiter(content));
        ps.close();
        if( FileSupport.write(out.toByteArray(), target) ) {
            progress("Wrote: "+target);
        }
    }
    
    @SuppressWarnings("unchecked")
    private <T> T[] array(Class<T> type, ArrayList<T> urls) {
        return urls.toArray((T[])Array.newInstance(type, urls.size()));
    }

    private String url(File file) throws IOException {
        return "file:"+(file.getCanonicalPath().replace(" ", "%20"));
    }
    
    @SuppressWarnings("serial")
    public static class UsageException extends Exception {
        public UsageException(String message) {
            super(message);
        }
    }

    private File nativeFile(String suffix) {
        return new File(nativeOutput, name+suffix);
    }

    public String getCopyright() {
        if (copyright == null)
            return "";
        
        int index = copyright.indexOf(END_YEAR_TAG);
        if (index != -1) {
            String temp = copyright.substring(0, index);
            temp += Calendar.getInstance().get(Calendar.YEAR);
            temp += copyright.substring(index + END_YEAR_TAG.length());
            copyright = temp;
        }
        
        return copyright;
    }

}
