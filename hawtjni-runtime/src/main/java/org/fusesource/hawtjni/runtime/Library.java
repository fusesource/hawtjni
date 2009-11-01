/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.fusesource.hawtjni.runtime;

import java.io.*;

public class Library {

    static final String SEPARATOR = System.getProperty("line.separator");
    static final String DELIMITER = System.getProperty("file.separator");

    static final String SUFFIX_64 = "-64"; 
    static final String DIR_32 = "lib-32";
    static final String DIR_64 = "lib-64";

    String name;
    int majorVersion = 1;
    int minorVersion = 0;
    int revision = 0;
    String platform;
    int bitModel;
    
    public Library(String name, int majorVersion, int minorVersion, int revision, String platform) {
        this(name, majorVersion, minorVersion, revision, platform, jvmBitModel());
    }
    
    public Library(String name, int majorVersion, int minorVersion, int revision, String platform, int bitModel) {
        this.name = name;
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.revision = revision;
        this.platform = platform;
        this.bitModel=bitModel;
    }

    private static int jvmBitModel() {
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
     * Loads the shared library that matches the version of the Java code which
     * is currently running. shared libraries follow an encoding scheme
     * where the major, minor and revision numbers are embedded in the library
     * name and this along with <code>name</code> is used to load the library.
     * If this fails, <code>name</code> is used in another attempt to load the
     * library, this time ignoring the version encoding scheme.
     * 
     * @param name
     *            the name of the library to load
     */
    public void load() {
        loadLibrary(true);
    }

    /**
     * Loads the shared library that matches the version of the Java code which
     * is currently running. shared libraries follow an encoding scheme
     * where the major, minor and revision numbers are embedded in the library
     * name and this along with <code>name</code> is used to load the library.
     * If this fails, <code>name</code> is used in another attempt to load the
     * library, this time ignoring the version encoding scheme.
     * 
     * @param name
     *            the name of the library to load
     * @param mapName
     *            true if the name should be mapped, false otherwise
     */
    private void loadLibrary(boolean mapName) {
        
        int jvmBitModel = jvmBitModel();
        if ( jvmBitModel > 0 ) {
            if ( jvmBitModel != bitModel ) { 
                throw new UnsatisfiedLinkError("Cannot load "+bitModel+"-bit libraries on "+jvmBitModel+"-bit JVM"); 
            }
        }

        /* Compute the library name and mapped name */
        String libName1, libName2, mappedName1, mappedName2;
        if (mapName) {
            String version = System.getProperty(name+".version"); 
            if (version == null) {
                version = "" + majorVersion; 
                /* Force 3 digits in minor version number */
                if (minorVersion < 10) {
                    version += "00"; 
                } else {
                    if (minorVersion < 100)
                        version += "0"; 
                }
                version += minorVersion;
                /* No "r" until first revision */
                if (revision > 0)
                    version += "r" + revision; 
            }
            libName1 = name + "-" + platform + "-" + version;  //$NON-NLS-2$
            libName2 = name + "-" + platform; 
            mappedName1 = mapLibraryName(libName1);
            mappedName2 = mapLibraryName(libName2);
        } else {
            libName1 = libName2 = mappedName1 = mappedName2 = name;
        }

        StringBuffer message = new StringBuffer();

        /* Try loading library from library path */
        String path = System.getProperty(name+".library.path"); 
        if (path != null) {
            path = new File(path).getAbsolutePath();
            if (load(path + SEPARATOR + mappedName1, message))
                return;
            if (mapName && load(path + SEPARATOR + mappedName2, message))
                return;
        }

        /* Try loading library from java library path */
        if (load(libName1, message))
            return;
        if (mapName && load(libName2, message))
            return;

        /*
         * Try loading library from the tmp directory if library path is not
         * specified
         */
        String fileName1 = mappedName1;
        String fileName2 = mappedName2;
        if (path == null) {
            path = System.getProperty("java.io.tmpdir"); 
            File dir = new File(path, bitModel==64 ? DIR_64 : DIR_32);
            boolean make = false;
            if ((dir.exists() && dir.isDirectory()) || (make = dir.mkdir())) {
                path = dir.getAbsolutePath();
                if (make)
                    chmod("777", path); 
            } else {
                /* fall back to using the tmp directory */
                if (bitModel==64) {
                    fileName1 = mapLibraryName(libName1 + SUFFIX_64);
                    fileName2 = mapLibraryName(libName2 + SUFFIX_64);
                }
            }
            if (load(path + SEPARATOR + fileName1, message))
                return;
            if (mapName && load(path + SEPARATOR + fileName2, message))
                return;
        }

        /* Try extracting and loading library from jar */
        if (path != null) {
            if (extract(path + SEPARATOR + fileName1, mappedName1, message))
                return;
            if (mapName && extract(path + SEPARATOR + fileName2, mappedName2, message))
                return;
        }

        /* Failed to find the library */
        throw new UnsatisfiedLinkError("Could not load library. Reasons: " + message.toString()); 
    }

    private String mapLibraryName(String libName) {
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

    private boolean extract(String fileName, String mappedName, StringBuffer message) {
        FileOutputStream os = null;
        InputStream is = null;
        File file = new File(fileName);
        boolean extracted = false;
        try {
            if (!file.exists()) {
                is = Library.class.getResourceAsStream("/" + mappedName); 
                if (is != null) {
                    extracted = true;
                    int read;
                    byte[] buffer = new byte[4096];
                    os = new FileOutputStream(fileName);
                    while ((read = is.read(buffer)) != -1) {
                        os.write(buffer, 0, read);
                    }
                    os.close();
                    is.close();
                    chmod("755", fileName);
                    if (load(fileName, message))
                        return true;
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
            if (extracted && file.exists())
                file.delete();
        }
        return false;
    }

    private void chmod(String permision, String path) {
        if (platform.equals("win32"))
            return; 
        try {
            Runtime.getRuntime().exec(new String[] { "chmod", permision, path }).waitFor(); 
        } catch (Throwable e) {
        }
    }

    private boolean load(String libName, StringBuffer message) {
        try {
            if (libName.indexOf(SEPARATOR) != -1) {
                System.load(libName);
            } else {
                System.loadLibrary(libName);
            }
            return true;
        } catch (UnsatisfiedLinkError e) {
            if (message.length() == 0)
                message.append(DELIMITER);
            message.append('\t');
            message.append(e.getMessage());
            message.append(DELIMITER);
        }
        return false;
    }

}
