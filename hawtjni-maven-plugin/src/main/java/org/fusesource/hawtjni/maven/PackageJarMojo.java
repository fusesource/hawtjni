/**
 * Copyright (C) 2009-2011 FuseSource Corp.
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
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
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
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
@Mojo(name = "package-jar", defaultPhase = LifecyclePhase.PREPARE_PACKAGE)
public class PackageJarMojo extends AbstractMojo {

    /**
     * The maven project.
     */
    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject project;

    /**
     * The base name of the library, used to determine generated file names.
     */
    @Parameter(defaultValue = "${project.artifactId}")
    private String name;

    /**
     */
    @Component
    private ArchiverManager archiverManager;

    /**
     */
    @Component
    private MavenProjectHelper projectHelper;

    /**
     * The output directory where the built JNI library will placed. This
     * directory will be added to as a test resource path so that unit tests can
     * verify the built JNI library.
     * 
     * The library will placed under the META-INF/native/${platform} directory
     * that the HawtJNI Library uses to find JNI libraries as classpath
     * resources.
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-sources/hawtjni/lib")
    private File libDirectory;
    
    /**
     * The platform identifier of this build.  If not specified,
     * it will be automatically detected.
     * 
     * @parameter
     */
    @Parameter
    private String platform;     

    /**
     * Should a classifier of the native jar be set
     * to match the platform?
     */
    @Parameter(defaultValue = "true")
    private boolean classified;

    /**
     * The osgi platforms that the library match for.  Example value:
     * osname=MacOS;processor=x86-64
     */
    @Parameter
    private List<String> osgiPlatforms;

    public void execute() throws MojoExecutionException {
        try {

            Library library = new Library(name);
            if (platform == null) {
                platform = Library.getPlatform();
            }

            String classifier = null;
            if( classified ) {
                classifier = platform;

                String packageName = project.getArtifactId() + "-" + project.getVersion() + "-" + platform;
                JarArchiver archiver = (JarArchiver) archiverManager.getArchiver("jar");

                File packageFile = new File(new File(project.getBuild().getDirectory()), packageName + ".jar");
                archiver.setDestFile(packageFile);
                archiver.setIncludeEmptyDirs(true);
                archiver.addDirectory(libDirectory);

                Manifest manifest = new Manifest();
                manifest.addConfiguredAttribute(new Attribute("Bundle-SymbolicName", project.getArtifactId() + "-" + platform));
                manifest.addConfiguredAttribute(new Attribute("Bundle-Name", name + " for " + platform));
                manifest.addConfiguredAttribute(new Attribute("Bundle-NativeCode", getNativeCodeValue(library)));
                manifest.addConfiguredAttribute(new Attribute("Bundle-Version", project.getVersion()));
                manifest.addConfiguredAttribute(new Attribute("Bundle-ManifestVersion", "2"));
                manifest.addConfiguredAttribute(new Attribute("Bundle-Description", project.getDescription()));
                archiver.addConfiguredManifest(manifest);

                archiver.createArchive();

                projectHelper.attachArtifact(project, "jar", classifier, packageFile);

            } else {
                projectHelper.addResource(project, libDirectory.getCanonicalPath(), null, null);
            }

        } catch (Exception e) {
            throw new MojoExecutionException("packaging failed: " + e, e);
        }
    }
    
    public String getNativeCodeValue(Library library) {
        if (osgiPlatforms == null || osgiPlatforms.isEmpty() ) {
            return library.getPlatformSpecificResourcePath(platform) + ";" +"osname=" + getOsgiOSName() + ";processor=" + getOsgiProcessor()+ ",*";
        }
        boolean first=true;
        String rc = "";
        for (String s : osgiPlatforms) {
            if( !first ) {
                rc += ",";
            }
            first = false;
            if( "*".equals(s) ) {
                rc += s;
            } else {
                rc += library.getPlatformSpecificResourcePath(platform) + ";"+s;
            }
        }
        return rc;
    }

    public String getOsgiOSName() {
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

    public String getOsgiProcessor() {
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

}
