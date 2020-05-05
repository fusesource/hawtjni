/*******************************************************************************
 * Copyright (C) 2009-2011 FuseSource Corp.
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.fusesource.hawtjni.runtime;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

/**
 * Used to find and load a JNI library, eventually after having extracted it.
 *
 * It will search for the library in order at the following locations:
 * <ol>
 * <li> in the custom library path: If the "<code>library.${name}.path</code>" System property is set to a directory,
 * subdirectories are searched:
 *   <ol>
 *   <li> "<code>${platform}/${arch}</code>"
 *   <li> "<code>${platform}</code>"
 *   <li> "<code>${os}</code>"
 *   </ol>
 *   for 2 namings of the library:
 *   <ol>
 *   <li> as "<code>${name}-${version}</code>" library name if the version can be determined.
 *   <li> as "<code>${name}</code>" library name
 *   </ol>
 * <li> system library path: This is where the JVM looks for JNI libraries by default.
 *   <ol>
 *   <li> as "<code>${name}${bit-model}-${version}</code>" library name if the version can be determined.
 *   <li> as "<code>${name}-${version}</code>" library name if the version can be determined.
 *   <li> as "<code>${name}</code>" library name
 *   </ol>
 * <li> classpath path: If the JNI library can be found on the classpath, it will get extracted
 * and then loaded. This way you can embed your JNI libraries into your packaged JAR files.
 * They are looked up as resources in this order:
 *   <ol>
 *   <li> "<code>META-INF/native/${platform}/${arch}/${library[-version]}</code>": Store your library here if you want to embed
 *   more than one platform JNI library on different processor archs in the jar.
 *   <li> "<code>META-INF/native/${platform}/${library[-version]}</code>": Store your library here if you want to embed more
 *   than one platform JNI library in the jar.
 *   <li> "<code>META-INF/native/${os}/${library[-version]}</code>": Store your library here if you want to embed more
 *   than one platform JNI library in the jar but don't want to take bit model into account.
 *   <li> "<code>META-INF/native/${library[-version]}</code>": Store your library here if your JAR is only going to embedding one
 *   platform library.
 *   </ol>
 * The file extraction is attempted until it succeeds in the following directories.
 *   <ol>
 *   <li> The directory pointed to by the "<code>library.${name}.path</code>" System property (if set)
 *   <li> a temporary directory (uses the "<code>java.io.tmpdir</code>" System property)
 *   </ol>
 * </ol>
 *
 * where:
 * <ul>
 * <li>"<code>${name}</code>" is the name of library
 * <li>"<code>${version}</code>" is the value of "<code>library.${name}.version</code>" System property if set.
 *       Otherwise it is set to the ImplementationVersion property of the JAR's Manifest</li>
 * <li>"<code>${os}</code>" is your operating system, for example "<code>osx</code>", "<code>linux</code>", or "<code>windows</code>"</li>
 * <li>"<code>${bit-model}</code>" is "<code>64</code>" if the JVM process is a 64 bit process, otherwise it's "<code>32</code>" if the
 * JVM is a 32 bit process</li>
 * <li>"<code>${arch}</code>" is the architecture for the processor, for example "<code>amd64</code>" or "<code>sparcv9</code>"</li>
 * <li>"<code>${platform}</code>" is "<code>${os}${bit-model}</code>", for example "<code>linux32</code>" or "<code>osx64</code>" </li>
 * <li>"<code>${library[-version]}</code>": is the normal jni library name for the platform (eventually with <code>-${version}</code>) suffix.
 *   For example "<code>${name}.dll</code>" on
 *   windows, "<code>lib${name}.jnilib</code>" on OS X, and "<code>lib${name}.so</code>" on linux</li>
 * </ul>
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 * @see System#mapLibraryName(String)
 */
public class Library {

    public static final String STRATEGY_PROPERTY = "hawtjni.strategy";
    public static final String STRATEGY_SHA1 = "sha1";
    public static final String STRATEGY_TEMP = "temp";

    static final String SLASH = System.getProperty("file.separator");

    static final String STRATEGY = System.getProperty(STRATEGY_PROPERTY,
            "windows".equals(getOperatingSystem()) ? STRATEGY_SHA1 : STRATEGY_TEMP);

    final private String name;
    final private String version;
    final private ClassLoader classLoader;
    private boolean loaded;
    private String nativeLibraryPath;
    private URL nativeLibrarySourceUrl;

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

    /**
     * Get the path to the native library loaded.
     * @return the path (should not be null once the library is loaded)
     * @since 1.16
     */
    public String getNativeLibraryPath() {
        return nativeLibraryPath;
    }

    /**
     * Get the URL to the native library source that has been extracted (if it was extracted).
     * @return the url to the source (in classpath)
     * @since 1.16
     */
    public URL getNativeLibrarySourceUrl() {
        return nativeLibrarySourceUrl;
    }

    public static String getOperatingSystem() {
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

    public static String getPlatform() {
        return getOperatingSystem()+getBitModel();
    }

    public static int getBitModel() {
        String prop = System.getProperty("sun.arch.data.model");
        if (prop == null) {
            prop = System.getProperty("com.ibm.vm.bitmode");
        }
        if( prop!=null ) {
            return Integer.parseInt(prop);
        }
        // GraalVM support, see https://github.com/fusesource/jansi/issues/162
        String arch = System.getProperty("os.arch");
        if (arch.endsWith("64") && "Substrate VM".equals(System.getProperty("java.vm.name"))) {
            return 64;
        }
        return -1; // we don't know..
    }

    /**
     * Load the native library.
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
        ArrayList<Throwable> errors = new ArrayList<Throwable>();

        String[] specificDirs = getSpecificSearchDirs();
        String libFilename = map(name);
        String versionlibFilename = (version == null) ? null : map(name + "-" + version);

        /* Try loading library from a custom library path */
        String customPath = System.getProperty("library."+name+".path");
        if (customPath != null) {
            for ( String dir: specificDirs ) {
                if( version!=null && load(errors, file(customPath, dir, versionlibFilename)) )
                    return;
                if( load(errors, file(customPath, dir, libFilename)) )
                    return;
            }
        }

        /* Try loading library from java library path */
        if( version!=null && loadLibrary(errors, name + getBitModel() + "-" + version) )
            return;
        if( version!=null && loadLibrary(errors, name + "-" + version) )
            return;
        if( loadLibrary(errors, name) )
            return;


        /* Try extracting the library from the jar */
        if( classLoader!=null ) {
            String targetLibName = version != null ? versionlibFilename : libFilename;
            for ( String dir: specificDirs ) {
                if( version!=null && extractAndLoad(errors, customPath, dir, versionlibFilename, targetLibName) )
                    return;
                if( extractAndLoad(errors, customPath, dir, libFilename, targetLibName) )
                    return;
            }
        }

        /* Failed to find the library */
        UnsatisfiedLinkError e  = new UnsatisfiedLinkError("Could not load library. Reasons: " + errors.toString());
        try {
            Method method = Throwable.class.getMethod("addSuppressed", Throwable.class);
            for (Throwable t : errors) {
                method.invoke(e, t);
            }
        } catch (Throwable ignore) {
        }
        throw e;
    }

    @Deprecated
    final public String getArchSpecifcResourcePath() {
        return getArchSpecificResourcePath();
    }
    final public String getArchSpecificResourcePath() {
        return "META-INF/native/"+ getPlatform() + "/" + System.getProperty("os.arch") + "/" +map(name);
    }

    @Deprecated
    final public String getOperatingSystemSpecifcResourcePath() {
        return getOperatingSystemSpecificResourcePath();
    }
    final public String getOperatingSystemSpecificResourcePath() {
        return getPlatformSpecificResourcePath(getOperatingSystem());
    }
    @Deprecated
    final public String getPlatformSpecifcResourcePath() {
        return getPlatformSpecificResourcePath();
    }
    final public String getPlatformSpecificResourcePath() {
        return getPlatformSpecificResourcePath(getPlatform());
    }
    @Deprecated
    final public String getPlatformSpecifcResourcePath(String platform) {
        return getPlatformSpecificResourcePath(platform);
    }
    final public String getPlatformSpecificResourcePath(String platform) {
        return "META-INF/native/"+platform+"/"+map(name);
    }

    @Deprecated
    final public String getResorucePath() {
        return getResourcePath();
    }
    final public String getResourcePath() {
        return "META-INF/native/"+map(name);
    }

    final public String getLibraryFileName() {
        return map(name);
    }

    /**
     * Search directories for library:<ul>
     * <li><code>${platform}/${arch}</code> to enable platform JNI library for different processor archs</li>
     * <li><code>${platform}</code> to enable platform JNI library</li>
     * <li><code>${os}</code> to enable OS JNI library</li>
     * <li>no directory</li>
     * </ul>
     * @return the list
     * @since 1.15
     */
    final public String[] getSpecificSearchDirs() {
        return new String[] {
                getPlatform() + "/" + System.getProperty("os.arch"),
                getPlatform(),
                getOperatingSystem(),
                "."
        };
    }

    private boolean extractAndLoad(ArrayList<Throwable> errors, String customPath, String dir, String libName, String targetLibName) {
        String resourcePath = "META-INF/native/" + ( dir == null ? "" : (dir + '/')) + libName;
        URL resource = classLoader.getResource(resourcePath);
        if( resource !=null ) {

            int idx = targetLibName.lastIndexOf('.');
            String prefix = targetLibName.substring(0, idx)+"-";
            String suffix = targetLibName.substring(idx);

            // Use the user provided path,
            // then fallback to the java temp directory,
            // and last, use the user home folder
            for (File path : Arrays.asList(
                                    customPath != null ? file(customPath) : null,
                                    file(System.getProperty("java.io.tmpdir")),
                                    file(System.getProperty("user.home"), ".hawtjni", name))) {
                if( path!=null ) {
                    // Try to extract it to the custom path...
                    File target;
                    if (STRATEGY_SHA1.equals(STRATEGY)) {
                        target = extractSha1(errors, resource, prefix, suffix, path);
                    } else {
                        target = extractTemp(errors, resource, prefix, suffix, path);
                    }
                    if( target!=null ) {
                        if( load(errors, target) ) {
                            nativeLibrarySourceUrl = resource;
                            return true;
                        }
                    }
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
            } else if( path != null ) {
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

    private File extractSha1(ArrayList<Throwable> errors, URL source, String prefix, String suffix, File directory) {
        File target = null;
        directory = directory.getAbsoluteFile();
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                errors.add(new IOException("Unable to create directory: " + directory));
                return null;
            }
        }
        try {
            String sha1 = computeSha1(source.openStream());
            String sha1f = "";
            target = new File(directory, prefix + sha1 + suffix);

            if (target.isFile() && target.canRead()) {
                sha1f = computeSha1(new FileInputStream(target));
            }
            if (sha1f.equals(sha1)) {
                return target;
            }

            FileOutputStream os = null;
            InputStream is = null;
            try {
                is = source.openStream();
                if (is != null) {
                    byte[] buffer = new byte[4096];
                    os = new FileOutputStream(target);
                    int read;
                    while ((read = is.read(buffer)) != -1) {
                        os.write(buffer, 0, read);
                    }
                    chmod755(target);
                }
                return target;
            } finally {
                close(os);
                close(is);
            }
        } catch (Throwable e) {
            IOException io;
            if (target != null) {
                target.delete();
                io = new IOException("Unable to extract library from " + source + " to " + target);
            } else {
                io = new IOException("Unable to create temporary file in " + directory);
            }
            io.initCause(e);
            errors.add(io);
        }
        return null;
    }

    private String computeSha1(InputStream is) throws NoSuchAlgorithmException, IOException {
        String sha1;
        try {
            MessageDigest mDigest = MessageDigest.getInstance("SHA1");
            int read;
            byte[] buffer = new byte[4096];
            while ((read = is.read(buffer)) != -1) {
                mDigest.update(buffer, 0, read);
            }
            byte[] result = mDigest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : result) {
                sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            sha1 = sb.toString();
        } finally {
            close(is);
        }
        return sha1;
    }

    private File extractTemp(ArrayList<Throwable> errors, URL source, String prefix, String suffix, File directory) {
        File target = null;
        directory = directory.getAbsoluteFile();
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                errors.add(new IOException("Unable to create directory: " + directory));
                return null;
            }
        }
        try {
            FileOutputStream os = null;
            InputStream is = null;
            try {
                target = File.createTempFile(prefix, suffix, directory);
                is = source.openStream();
                if (is != null) {
                    byte[] buffer = new byte[4096];
                    os = new FileOutputStream(target);
                    int read;
                    while ((read = is.read(buffer)) != -1) {
                        os.write(buffer, 0, read);
                    }
                    chmod755(target);
                }
                target.deleteOnExit();
                return target;
            } finally {
                close(os);
                close(is);
            }
        } catch (Throwable e) {
            IOException io;
            if (target != null) {
                target.delete();
                io = new IOException("Unable to extract library from " + source + " to " + target);
            } else {
                io = new IOException("Unable to create temporary file in " + directory);
            }
            io.initCause(e);
            errors.add(io);
        }
        return null;
    }

    static private void close(Closeable file) {
        if (file != null) {
            try {
                file.close();
            } catch (Exception ignore) {
            }
        }
    }

    private void chmod755(File file) {
        if (getPlatform().startsWith("windows"))
            return;
        // Use Files.setPosixFilePermissions if we are running Java 7+ to avoid forking the JVM for executing chmod
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            // Check if the PosixFilePermissions exists in the JVM, if not this will throw a ClassNotFoundException
            Class<?> posixFilePermissionsClass = classLoader.loadClass("java.nio.file.attribute.PosixFilePermissions");
            // Set <PosixFilePermission> permissionSet = PosixFilePermissions.fromString("rwxr-xr-x")
            Method fromStringMethod = posixFilePermissionsClass.getMethod("fromString", String.class);
            Object permissionSet = fromStringMethod.invoke(null, "rwxr-xr-x");
            // Path path = file.toPath()
            Object path = file.getClass().getMethod("toPath").invoke(file);
            // Files.setPosixFilePermissions(path, permissionSet)
            Class<?> pathClass = classLoader.loadClass("java.nio.file.Path");
            Class<?> filesClass = classLoader.loadClass("java.nio.file.Files");
            Method setPosixFilePermissionsMethod = filesClass.getMethod("setPosixFilePermissions", pathClass, Set.class);
            setPosixFilePermissionsMethod.invoke(null, path, permissionSet);
        } catch (Throwable ignored) {
            // Fallback to starting a new process
            try {
                Runtime.getRuntime().exec(new String[]{"chmod", "755", file.getCanonicalPath()}).waitFor();
            } catch (Throwable e) {
            }
        }
    }

    private boolean load(ArrayList<Throwable> errors, File lib) {
        try {
            System.load(lib.getPath());
            nativeLibraryPath = lib.getPath();
            return true;
        } catch (UnsatisfiedLinkError e) {
            LinkageError le = new LinkageError("Unable to load library from " + lib);
            le.initCause(e);
            errors.add(le);
        }
        return false;
    }

    private boolean loadLibrary(ArrayList<Throwable> errors, String lib) {
        try {
            System.loadLibrary(lib);
            nativeLibraryPath = "java.library.path,sun.boot.library.pathlib:" + lib;
            return true;
        } catch (UnsatisfiedLinkError e) {
            LinkageError le = new LinkageError("Unable to load library " + lib);
            le.initCause(e);
            errors.add(le);
        }
        return false;
    }

}
