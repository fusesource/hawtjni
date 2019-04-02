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

import java.io.*;
import java.net.URL;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.fusesource.hawtjni.runtime.Library;

/**
 * This goal builds the JNI module which was previously
 * generated with the generate goal.  It adds the JNI module
 * to the test resource path so that unit tests can load 
 * the freshly built JNI library.
 * 
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
@Mojo(name = "build", defaultPhase = LifecyclePhase.GENERATE_TEST_RESOURCES)
public class BuildMojo extends AbstractMojo {

    /**
     * The maven project.
     */
    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject project;
    
    /**
     * Remote repositories
     */
    @Parameter(defaultValue = "${project.remoteArtifactRepositories}", readonly = true)
    protected List remoteArtifactRepositories;

    /**
     * Local maven repository.
     */
    @Parameter(defaultValue = "${localRepository}", readonly = true)
    protected ArtifactRepository localRepository;

    /**
     * Artifact factory, needed to download the package source file
     */
    @Component
    protected ArtifactFactory artifactFactory;

    /**
     * Artifact resolver, needed to download the package source file
     */
    @Component
    protected ArtifactResolver artifactResolver;
    
    /**
     */
    @Component
    private ArchiverManager archiverManager;    

    /**
     * The base name of the library, used to determine generated file names.
     */
    @Parameter(defaultValue = "${project.artifactId}")
    private String name;
    
    /**
     * Where the unpacked build package is located.
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-sources/hawtjni/native-package")
    private File packageDirectory;

    /**
     * The output directory where the built JNI library will placed.  This directory will be added
     * to as a test resource path so that unit tests can verify the built JNI library.
     * 
     * The library will placed under the META-INF/native/${platform} directory that the HawtJNI
     * Library uses to find JNI libraries as classpath resources.
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-sources/hawtjni/lib")
    private File libDirectory;

    /**
     * The directory where the build will be produced.  It creates a native-build and native-dist directory
     * under the specified directory.
     */
    @Parameter(defaultValue = "${project.build.directory}")
    private File buildDirectory;

    /**
     * Should we skip executing the autogen.sh file.
     */
    @Parameter(defaultValue = "${skip-autogen}")
    private boolean skipAutogen;
    
    /**
     * Should we force executing the autogen.sh file.
     */
    @Parameter(defaultValue = "${force-autogen}")
    private boolean forceAutogen;
    
    /**
     * Extra arguments you want to pass to the autogen.sh command.
     */
    @Parameter
    private List<String> autogenArgs;

    /**
     * Should we skip executing the configure command.
     */
    @Parameter(defaultValue = "${skip-configure}")
    private boolean skipConfigure;

    /**
     * Should we force executing the configure command.
     */
    @Parameter(defaultValue = "${force-configure}")
    private boolean forceConfigure;
    
    /**
     * Should we display all the native build output?
     */
    @Parameter(defaultValue = "${hawtjni-verbose}")
    private boolean verbose;

    /**
     * Extra arguments you want to pass to the configure command.
     */
    @Parameter
    private List<String> configureArgs;
    
    /**
     * The platform identifier of this build.  If not specified,
     * it will be automatically detected.
     */
    @Parameter
    private String platform;    
    
    /**
     * The classifier of the package archive that will be created.
     */
    @Parameter(defaultValue = "native-src")
    private String sourceClassifier;  
    
    /**
     * If the source build could not be fully generated, perhaps the autotools
     * were not available on this platform, should we attempt to download
     * a previously deployed source package and build that?
     */
    @Parameter(defaultValue = "true")
    private boolean downloadSourcePackage = true;  

    /**
     * The dependency to download to get the native sources.
     */
    @Parameter
    private Dependency nativeSrcDependency;

    /**
     * URL to where we can down the source package
     */
    @Parameter(defaultValue = "${native-src-url}")
    private String nativeSrcUrl;

    /**
     * The build tool to use on Windows systems.  Set
     * to 'msbuild', 'vcbuild', or 'detect'
     */
    @Parameter(defaultValue = "detect")
    private String windowsBuildTool;

    /**
     * The name of the msbuild/vcbuild project to use.
     * Defaults to 'vs2010' for 'msbuild'
     * and 'vs2008' for 'vcbuild'.
     */
    @Parameter
    private String windowsProjectName;

    private final CLI cli = new CLI();

    public void execute() throws MojoExecutionException {
    	cli.verbose = verbose;
    	cli.log = getLog();
        try {
            File buildDir = new File(buildDirectory, "native-build");
            buildDir.mkdirs();
            if ( CLI.IS_WINDOWS ) {
                vsBasedBuild(buildDir);
            } else {
                configureBasedBuild(buildDir);
            }
            
            getLog().info("Adding test resource root: "+libDirectory.getAbsolutePath());
            Resource testResource = new Resource();
            testResource.setDirectory(libDirectory.getAbsolutePath());
            this.project.addTestResource(testResource); //();
            
        } catch (Exception e) {
            throw new MojoExecutionException("build failed: "+e, e);
        } 
    }

    private void vsBasedBuild(File buildDir) throws CommandLineException, MojoExecutionException, IOException {
    	
        FileUtils.copyDirectoryStructureIfModified(packageDirectory, buildDir);

        Library library = new Library(name);
        String libPlatform = this.platform != null ? this.platform : Library.getPlatform();
        String platform;
        String configuration="release";
        if( "windows32".equals(libPlatform) ) {
        	platform = "Win32";
        } else if( "windows64".equals(libPlatform) ) {
        	platform = "x64";
        } else {
        	throw new MojoExecutionException("Unsupported platform: "+libPlatform);
        }

        boolean useMSBuild = false;
        String tool = windowsBuildTool.toLowerCase().trim();
        if( "detect".equals(tool) ) {
            String toolset = System.getenv("PlatformToolset");
            if( "Windows7.1SDK".equals(toolset) ) {
                useMSBuild = true;
            } else {
                String vcinstalldir = System.getenv("VCINSTALLDIR");
                if( vcinstalldir!=null ) {
                    if( vcinstalldir.contains("Microsoft Visual Studio 10") ||
                        vcinstalldir.contains("Microsoft Visual Studio 11") ||
                        vcinstalldir.contains("Microsoft Visual Studio 12") ||
                        vcinstalldir.contains("Microsoft Visual Studio 14") ||
                        vcinstalldir.contains("Microsoft Visual Studio\\2017")
                      ) {
                        useMSBuild = true;
                    }
                }
            }
        } else if( "msbuild".equals(tool) ) {
            useMSBuild = true;
        } else if( "vcbuild".equals(tool) ) {
            useMSBuild = false;
        } else {
            throw new MojoExecutionException("Invalid setting for windowsBuildTool: "+windowsBuildTool);
        }

        if( useMSBuild ) {
            // vcbuild was removed.. use the msbuild tool instead.
            int rc = cli.system(buildDir, new String[]{"msbuild", (windowsProjectName != null ? windowsProjectName : "vs2010") + ".vcxproj", "/property:Platform="+platform, "/property:Configuration="+configuration});
            if( rc != 0 ) {
                throw new MojoExecutionException("vcbuild failed with exit code: "+rc);
            }
        } else {
            // try to use a vcbuild..
            int rc = cli.system(buildDir, new String[]{"vcbuild", "/platform:"+platform, (windowsProjectName != null ? windowsProjectName : "vs2008") + ".vcproj", configuration});
            if( rc != 0 ) {
                throw new MojoExecutionException("vcbuild failed with exit code: "+rc);
            }
        }

        File libFile=FileUtils.resolveFile(buildDir, "target/"+platform+"-"+configuration+"/lib/"+library.getLibraryFileName());
        if( !libFile.exists() ) {
            throw new MojoExecutionException("vcbuild did not generate: "+libFile);
        }        

        File target=FileUtils.resolveFile(libDirectory, library.getPlatformSpecificResourcePath(libPlatform));
        FileUtils.copyFile(libFile, target);

	}

    
	private void configureBasedBuild(File buildDir) throws IOException, MojoExecutionException, CommandLineException {
        
        File configure = new File(packageDirectory, "configure");
        if( configure.exists() ) {
            FileUtils.copyDirectoryStructureIfModified(packageDirectory, buildDir);            
        } else if (downloadSourcePackage) {
            downloadNativeSourcePackage(buildDir);
        } else {
            if( !buildDir.exists() ) {
                throw new MojoExecutionException("The configure script is missing from the generated native source package and downloadSourcePackage is disabled: "+configure);
            }
        }

        configure = new File(buildDir, "configure");
        File autogen = new File(buildDir, "autogen.sh");
        File makefile = new File(buildDir, "Makefile");
        
        File distDirectory = new File(buildDir, "target");
        File distLibDirectory = new File(distDirectory, "lib");
        distLibDirectory.mkdirs();
        
        if( autogen.exists() && !skipAutogen ) {
            if( (!configure.exists() && !CLI.IS_WINDOWS) || forceAutogen ) {
                cli.setExecutable(autogen);
                int rc = cli.system(buildDir, new String[] {"./autogen.sh"}, autogenArgs);
                if( rc != 0 ) {
                    throw new MojoExecutionException("./autogen.sh failed with exit code: "+rc);
                }
            }
        }
        
        if( configure.exists() && !skipConfigure ) {
            if( !makefile.exists() || forceConfigure ) {
                
                File autotools = new File(buildDir, "autotools");
                File[] listFiles = autotools.listFiles();
                if( listFiles!=null ) {
                    for (File file : listFiles) {
                        cli.setExecutable(file);
                    }
                }
                
                cli.setExecutable(configure);
                int rc = cli.system(buildDir, new String[]{"./configure", "--disable-ccache", "--prefix="+distDirectory.getCanonicalPath(), "--libdir="+distDirectory.getCanonicalPath()+"/lib"}, configureArgs);
                if( rc != 0 ) {
                    throw new MojoExecutionException("./configure failed with exit code: "+rc);
                }
            }
        }
        
        int rc = cli.system(buildDir, new String[]{"make", "install"});
        if( rc != 0 ) {
            throw new MojoExecutionException("make based build failed with exit code: "+rc);
        }
        
        Library library = new Library(name);
        
        File libFile = new File(distLibDirectory, library.getLibraryFileName());
        if( !libFile.exists() ) {
            throw new MojoExecutionException("Make based build did not generate: "+libFile);
        }
        
        if( platform == null ) {
            platform = library.getPlatform();
        }
        
        File target=FileUtils.resolveFile(libDirectory, library.getPlatformSpecificResourcePath(platform));
        FileUtils.copyFile(libFile, target);
    }
    
    public void downloadNativeSourcePackage(File buildDir) throws MojoExecutionException  {
        File packageZipFile;
        if( nativeSrcUrl ==null || nativeSrcUrl.trim().length()==0 ) {
            Artifact artifact=null;
            if( nativeSrcDependency==null ) {
                artifact = artifactFactory.createArtifactWithClassifier(project.getGroupId(), project.getArtifactId(), project.getVersion(), "zip", sourceClassifier);
            } else {
                artifact = artifactFactory.createArtifactWithClassifier(nativeSrcDependency.getGroupId(), nativeSrcDependency.getArtifactId(), nativeSrcDependency.getVersion(), nativeSrcDependency.getType(), nativeSrcDependency.getClassifier());
            }
            try {
                artifactResolver.resolveAlways(artifact, remoteArtifactRepositories, localRepository);
            } catch (ArtifactResolutionException e) {
                throw new MojoExecutionException("Error downloading.", e);
            } catch (ArtifactNotFoundException e) {
                throw new MojoExecutionException("Requested download does not exist.", e);
            }

            packageZipFile = artifact.getFile();
            if( packageZipFile.isDirectory() ) {
                // Yep. looks like we are running on mvn 3, seem like
                // mvn 3 does not actually download the artifact. it just points us
                // to our own build.
                throw new MojoExecutionException("Add a '-Dnative-src-url=file:...' to have maven download the native package");
            }
        } else {
            try {
                packageZipFile = new File(buildDirectory, "native-build.zip");
                URL url = new URL(nativeSrcUrl.trim());
                InputStream is = url.openStream();
                try {
                    FileOutputStream os = new FileOutputStream(packageZipFile);
                    try {
                        IOUtil.copy(is, os);
                    } finally {
                        IOUtil.close(is);
                    }

                } finally {
                    IOUtil.close(is);
                }
            } catch (Exception e) {
                throw new MojoExecutionException("Error downloading: "+ nativeSrcUrl, e);
            }
        }

        try {
            File dest = new File(buildDirectory, "native-build-extracted");
            getLog().info("Extracting "+packageZipFile+" to "+dest);
            
            UnArchiver unArchiver = archiverManager.getUnArchiver("zip");
            unArchiver.setSourceFile(packageZipFile);
            unArchiver.extract("", dest);


            File source = findSourceRoot(dest);
            if( source==null ) {
                throw new MojoExecutionException("Extracted package did not look like it contained a native source build.");
            }
            FileUtils.copyDirectoryStructureIfModified(source, buildDir);            
            
        } catch (MojoExecutionException e) {
            throw e;
        } catch (Throwable e) {
            throw new MojoExecutionException("Could not extract the native source package.", e);
        }            
    }

    private File findSourceRoot(File dest) {
        if(dest.isDirectory()) {
            if( new File(dest, "configure").exists() ) {
                return dest;
            } else {
                for (File file : dest.listFiles()) {
                    File root = findSourceRoot(file);
                    if( root!=null ) {
                        return root;
                    }
                }
                return null;
            }
        } else {
            return null;
        }
    }

}
