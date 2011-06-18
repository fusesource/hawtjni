/*******************************************************************************
 * Copyright (c) 2009 Progress Software, Inc.
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.fusesource.hawtjni.runtime;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Used to optionally extract and load a JNI library.
 * 
 * It will search for the library in order at the following locations:
 * <ol>
 * <li> in the custom library path: If the "library.${name}.path" System property is set to a directory 
 *   <ol>
 *   <li> "${name}-${version}" if the version can be determined.
 *   <li> "${name}"
 *   </ol>
 * <li> system library path: This is where the JVM looks for JNI libraries by default.
 *   <ol>
 *   <li> "${name}-${version}" if the version can be determined.
 *   <li> "${name}"
 *   </ol>
 * <li> classpath path: If the JNI library can be found on the classpath, it will get extracted
 * and and then loaded.  This way you can embed your JNI libraries into your packaged JAR files.
 * They are looked up as resources in this order:
 *   <ol>
 *   <li> "META-INF/native/${platform}/${library}" : Store your library here if you want to embed more
 *   than one platform JNI library in the jar.
 *   <li> "META-INF/native/${library}": Store your library here if your JAR is only going to embedding one
 *   platform library.
 *   </ol>
 * The file extraction is attempted until it succeeds in the following directories.
 *   <ol>
 *   <li> The directory pointed to by the "library.${name}.path" System property (if set)
 *   <li> a temporary directory (uses the "java.io.tmpdir" System property)
 *   </ol>
 * </ol>
 * 
 * where: 
 * <ul>
 * <li>"${name}" is the name of library
 * <li>"${version}" is the value of "library.${name}.version" System property if set.
 *       Otherwise it is set to the ImplementationVersion property of the JAR's Manifest</li> 
 * <li>"${os}" is your operating system, for example "osx", "linux", or "windows"</li> 
 * <li>"${bit-model}" is "64" if the JVM process is a 64 bit process, otherwise it's "32" if the 
 * JVM is a 32 bit process</li> 
 * <li>"${platform}" is "${os}${bit-model}", for example "linux32" or "osx64" </li> 
 * <li>"${library}": is the normal jni library name for the platform.  For example "${name}.dll" on
 *     windows, "lib${name}.jnilib" on OS X, and "lib${name}.so" on linux</li> 
 * </ul>
 * 
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class Library {

    static final String SLASH = System.getProperty("file.separator");

    final private String name;
    final private String version;
    final private ClassLoader classLoader;
    private boolean loaded;
    
    public Library(String name) {
        this(name, null, null);
    }
    
    public Library(String name, Class<?> clazz) {
        this(name, version(clazz), clazz.getClassLoader());
    }
    
    public Library(String name, String version) {
        this(name, version, null);
    }
    
    public Library(String name, String version, ClassLoader classLoader) {
        if( name == null ) {
            throw new IllegalArgumentException("name cannot be null");
        }
        this.name = name;
        this.version = version;
        this.classLoader= classLoader;
    }
    
    private static String version(Class<?> clazz) {
        try {
            return clazz.getPackage().getImplementationVersion();
        } catch (Throwable e) {
        }
        return null;
    }

    public String getOperatingSystem() {
        String name = System.getProperty("os.name").toLowerCase().trim();
        if( name.startsWith("linux") ) {
            return "linux";
        }
        if( name.startsWith("mac os x") ) {
            return "osx";
        }
        if( name.startsWith("win") ) {
            return "windows";
        }
        return name.replaceAll("\\W+", "_");
        
    }

    public String getPlatform() {
        return getOperatingSystem()+getBitModel();
    }
    
    protected static int getBitModel() {
        String prop = System.getProperty("sun.arch.data.model"); 
        if (prop == null) {
            prop = System.getProperty("com.ibm.vm.bitmode");
        }
        if( prop!=null ) {
            return Integer.parseInt(prop);
        }
        return -1; // we don't know..  
    }

    /**
     * 
     */
    synchronized public void load() {
        if( loaded ) {
            return;
        }
        doLoad();
        loaded = true;
    }
    
    private void doLoad() {
        /* Perhaps a custom version is specified */
        String version = System.getProperty("library."+name+".version"); 
        if (version == null) {
            version = this.version; 
        }
        ArrayList<String> errors = new ArrayList<String>();

        /* Try loading library from a custom library path */
        String customPath = System.getProperty("library."+name+".path");
        if (customPath != null) {
            if( version!=null && load(errors, file(customPath, map(name + "-" + version))) ) 
                return;
            if( load(errors, file(customPath, map(name))) )
                return;
        }

        /* Try loading library from java library path */
        if( version!=null && load(errors, name + "-" + version) ) 
            return;        
        if( load(errors, name ) )
            return;
        
        
        /* Try extracting the library from the jar */
        if( classLoader!=null ) {
            if( exractAndLoad(errors, version, customPath, getPlatformSpecifcResourcePath()) ) 
                return;
            if( exractAndLoad(errors, version, customPath, getOperatingSystemSpecifcResourcePath()) ) 
                return;
            // For the simpler case where only 1 platform lib is getting packed into the jar
            if( exractAndLoad(errors, version, customPath, getResorucePath()) )
                return;
        }

        /* Failed to find the library */
        throw new UnsatisfiedLinkError("Could not load library. Reasons: " + errors.toString()); 
    }

    final public String getOperatingSystemSpecifcResourcePath() {
        return getPlatformSpecifcResourcePath(getOperatingSystem());
    }
    final public String getPlatformSpecifcResourcePath() {
        return getPlatformSpecifcResourcePath(getPlatform());
    }
    final public String getPlatformSpecifcResourcePath(String platform) {
        return "META-INF/native/"+platform+"/"+map(name);
    }

    final public String getResorucePath() {
        return "META-INF/native/"+map(name);
    }

    final public String getLibraryFileName() {
        return map(name);
    }

    
    private boolean exractAndLoad(ArrayList<String> errors, String version, String customPath, String resourcePath) {
        URL resource = classLoader.getResource(resourcePath);
        if( resource !=null ) {
            
            String libName = name;
            if( version !=null) {
                libName += "-" + version;
            }
            libName += "-" + getBitModel();
            
            if( customPath!=null ) {
                // Try to extract it to the custom path...
                File target = file(customPath, map(libName));
                if( extract(errors, resource, target) ) {
                    if( load(errors, target) ) {
                        return true;
                    }
                }
            }
            
            // Fall back to extracting to the tmp dir
            customPath = System.getProperty("java.io.tmpdir");
            File target = file(customPath, map(libName));
            if( extract(errors, resource, target) ) {
                if( load(errors, target) ) {
                    return true;
                }
            }
        }
        return false;
    }

    private File file(String ...paths) {
        File rc = null ;
        for (String path : paths) {
            if( rc == null ) {
                rc = new File(path);
            } else {
                rc = new File(rc, path);
            }
        }
        return rc;
    }
    
    private String map(String libName) {
        /*
         * libraries in the Macintosh use the extension .jnilib but the some
         * VMs map to .dylib.
         */
        libName = System.mapLibraryName(libName);
        String ext = ".dylib"; 
        if (libName.endsWith(ext)) {
            libName = libName.substring(0, libName.length() - ext.length()) + ".jnilib"; 
        }
        return libName;
    }

    private boolean extract(ArrayList<String> errors, URL source, File target) {
        FileOutputStream os = null;
        InputStream is = null;
        boolean extracting = false;
        try {
            if (!target.exists() || isStale(source, target) ) {
                is = source.openStream();
                if (is != null) {
                    byte[] buffer = new byte[4096];
                    os = new FileOutputStream(target);
                    extracting = true;
                    int read;
                    while ((read = is.read(buffer)) != -1) {
                        os.write(buffer, 0, read);
                    }
                    os.close();
                    is.close();
                    chmod("755", target);
                }
            }
        } catch (Throwable e) {
            try {
                if (os != null)
                    os.close();
            } catch (IOException e1) {
            }
            try {
                if (is != null)
                    is.close();
            } catch (IOException e1) {
            }
            if (extracting && target.exists())
                target.delete();
            errors.add(e.getMessage());
            return false;
        }
        return true;
    }

    private boolean isStale(URL source, File target) {
        
        if( source.getProtocol().equals("jar") ) {
            // unwrap the jar protocol...
            try {
                String parts[] = source.getFile().split(Pattern.quote("!"));
                source = new URL(parts[0]);
            } catch (MalformedURLException e) {
                return false;
            }
        }
        
        File sourceFile=null;
        if( source.getProtocol().equals("file") ) {
            sourceFile = new File(source.getFile());
        }
        if( sourceFile!=null && sourceFile.exists() ) {
            if( sourceFile.lastModified() > target.lastModified() ) {
                return true;
            }
        }
        return false;
    }

    private void chmod(String permision, File path) {
        if (getPlatform().startsWith("windows"))
            return; 
        try {
            Runtime.getRuntime().exec(new String[] { "chmod", permision, path.getCanonicalPath() }).waitFor(); 
        } catch (Throwable e) {
        }
    }

    private boolean load(ArrayList<String> errors, File lib) {
        try {
            System.load(lib.getPath());
            return true;
        } catch (UnsatisfiedLinkError e) {
            errors.add(e.getMessage());
        }
        return false;
    }
    
    private boolean load(ArrayList<String> errors, String lib) {
        try {
            System.loadLibrary(lib);
            return true;
        } catch (UnsatisfiedLinkError e) {
            errors.add(e.getMessage());
        }
        return false;
    }

}
