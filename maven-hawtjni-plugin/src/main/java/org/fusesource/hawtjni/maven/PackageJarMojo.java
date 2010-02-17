/**
 * Copyright (C) 2009 Progress Software, Inc.
 * http://fusesource.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusesource.hawtjni.maven;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.codehaus.plexus.archiver.jar.Manifest;
import org.codehaus.plexus.archiver.jar.Manifest.Attribute;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.fusesource.hawtjni.runtime.Library;

/**
 * This goal allows allows you to package the JNI library created by build goal
 * in a JAR which the HawtJNI runtime can unpack when the library needs to be
 * loaded.
 * 
 * This platform specific jar is attached with a classifier which matches the
 * current platform.
 * 
 * @goal package-jar
 * @phase package
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class PackageJarMojo extends AbstractMojo {

    /**
     * The maven project.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * The base name of the library, used to determine generated file names.
     * 
     * @parameter default-value="${project.artifactId}"
     */
    private String name;

    /**
     * @component
     * @required
     * @readonly
     */
    private ArchiverManager archiverManager;

    /**
     * @component
     * @required
     * @readonly
     */
    private MavenProjectHelper projectHelper;

    /**
     * The output directory where the built JNI library will placed. This
     * directory will be added to as a test resource path so that unit tests can
     * verify the built JNI library.
     * 
     * The library will placed under the META-INF/native/${platform} directory
     * that the HawtJNI Library uses to find JNI libraries as classpath
     * resources.
     * 
     * @parameter 
     *            default-value="${project.build.directory}/generated-sources/hawtjni/lib"
     */
    private File libDirectory;

    /**
     * The classifier of the jar will use. If not specified it will be set to
     * the platform string determined by the HawtJNI Library class.
     * 
     * @parameter
     */
    private String jarClassifier;

    /**
     * The osname to use in the OSGi bundle meta data.
     * 
     * @parameter
     */
    private String osgiOSName;

    /**
     * The processor to use in the OSGi bundle meta data.
     * 
     * @parameter
     */
    private String osgiProcessor;

    public void execute() throws MojoExecutionException {
        try {

            Library library = new Library(name);
            if (jarClassifier == null) {
                jarClassifier = library.getPlatform();
            }

            String packageName = project.getArtifactId() + "-" + project.getVersion() + "-" + jarClassifier;
            JarArchiver archiver = (JarArchiver) archiverManager.getArchiver("jar");

            File packageFile = new File(new File(project.getBuild().getDirectory()), packageName + ".jar");
            archiver.setDestFile(packageFile);
            archiver.setIncludeEmptyDirs(true);
            archiver.addDirectory(libDirectory);

            String osname = getOsgiOSName();
            String processor = getOsgiProcessor();
            if (osname != null && processor != null) {
                Manifest manifest = new Manifest();
                manifest.addConfiguredAttribute(new Attribute("Bundle-SymbolicName", project.getArtifactId() + "-" + jarClassifier));
                manifest.addConfiguredAttribute(new Attribute("Bundle-Name", name + " for " + osname + " on " + processor));
                manifest.addConfiguredAttribute(new Attribute("Bundle-NativeCode", library.getPlatformSpecifcResourcePath() + ";osname=" + osname + ";processor=" + processor
                        + ",*"));
                manifest.addConfiguredAttribute(new Attribute("Bundle-Version", project.getVersion()));
                manifest.addConfiguredAttribute(new Attribute("Bundle-ManifestVersion", "2"));
                manifest.addConfiguredAttribute(new Attribute("Bundle-Description", project.getDescription()));
                archiver.addConfiguredManifest(manifest);
            }

            archiver.createArchive();

            projectHelper.attachArtifact(project, "jar", jarClassifier, packageFile);

        } catch (Exception e) {
            throw new MojoExecutionException("packageing failed: " + e, e);
        }
    }

    public String getOsgiOSName() {
        if (osgiOSName == null) {
            String name = System.getProperty("os.name");

            String trimmed = name.toLowerCase().trim();
            if (trimmed.startsWith("win")) {
                return "Win32";
            } else if (trimmed.startsWith("linux")) {
                return "Linux";
            } else if (trimmed.startsWith("macos") || trimmed.startsWith("mac os")) {
                return "MacOS";
            } else if (trimmed.startsWith("aix")) {
                return "AIX";
            } else if (trimmed.startsWith("hpux")) {
                return "HPUX";
            } else if (trimmed.startsWith("irix")) {
                return "IRIX";
            } else if (trimmed.startsWith("netware")) {
                return "Netware";
            } else if (trimmed.startsWith("openbsd")) {
                return "OpenBSD";
            } else if (trimmed.startsWith("netbsd")) {
                return "NetBSD";
            } else if (trimmed.startsWith("os2") || trimmed.startsWith("os/2")) {
                return "OS2";
            } else if (trimmed.startsWith("qnx") || trimmed.startsWith("procnto")) {
                return "QNX";
            } else if (trimmed.startsWith("solaris")) {
                return "Solaris";
            } else if (trimmed.startsWith("sunos")) {
                return "SunOS";
            } else if (trimmed.startsWith("vxworks")) {
                return "VxWorks";
            }
            return name;
        }
        return osgiOSName;
    }

    public String getOsgiProcessor() {
        if (osgiProcessor == null) {
            String name = System.getProperty("os.arch");
            String trimmed = name.toLowerCase().trim();
            if (trimmed.startsWith("x86-64") || trimmed.startsWith("amd64") || trimmed.startsWith("em64") || trimmed.startsWith("x86_64")) {
                return "x86-64";
            } else if (trimmed.startsWith("x86") || trimmed.startsWith("pentium") || trimmed.startsWith("i386") 
                    || trimmed.startsWith("i486") || trimmed.startsWith("i586") || trimmed.startsWith("i686")) {
                return "x86";
            } else if (trimmed.startsWith("68k")) {
                return "68k";
            } else if (trimmed.startsWith("arm")) {
                return "ARM";
            } else if (trimmed.startsWith("alpha")) {
                return "Alpha";
            } else if (trimmed.startsWith("ignite") || trimmed.startsWith("psc1k")) {
                return "Ignite";
            } else if (trimmed.startsWith("mips")) {
                return "Mips";
            } else if (trimmed.startsWith("parisc")) {
                return "PArisc";
            } else if (trimmed.startsWith("powerpc") || trimmed.startsWith("power") || trimmed.startsWith("ppc")) {
                return "PowerPC";
            } else if (trimmed.startsWith("sparc")) {
                return "Sparc";
            }
            return name;
        }
        return osgiProcessor;
    }

}
