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
import java.io.IOException;
import java.util.List;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.Arg;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.fusesource.hawtjni.runtime.Library;

/**
 * This goal builds the JNI module which was previously
 * generated with the generate goal.  It adds the JNI module
 * to the test resource path so that unit tests can load 
 * the freshly built JNI library.
 * 
 * @goal build
 * @phase generate-test-resources
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class BuildMojo extends AbstractMojo {

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
     * Where the unpacked build package is located.
     * 
     * @parameter default-value="${project.build.directory}/generated-sources/hawtjni/native-package"
     */
    private File packageDirectory;

    /**
     * The output directory where the built JNI library will placed.  This directory will be added
     * to as a test resource path so that unit tests can verify the built JNI library.
     * 
     * The library will placed under the META-INF/native/${platform} directory that the HawtJNI
     * Library uses to find JNI libraries as classpath resources.
     * 
     * @parameter default-value="${project.build.directory}/generated-sources/hawtjni/lib"
     */
    private File libDirectory;

    /**
     * The directory where the build will be produced.  It creates a native-build and native-dist directory
     * under the specified directory.
     * 
     * @parameter default-value="${project.build.directory}"
     */
    private File buildDirectory;

    /**
     * Should we skip executing the autogen.sh file.
     * 
     * @parameter default-value="false"
     */
    private boolean skipAutogen;
    
    /**
     * Should we force executing the autogen.sh file.
     * 
     * @parameter default-value="false"
     */
    private boolean forceAutogen;
    
    /**
     * Extra arguments you want to pass to the autogen.sh command.
     * 
     * @parameter
     */
    private List<Arg> autogenArgs;

    /**
     * Should we skip executing the configure command.
     * 
     * @parameter default-value="false"
     */
    private boolean skipConfigure;

    /**
     * Should we force executing the configure command.
     * 
     * @parameter default-value="false"
     */
    private boolean forceConfigure;
    
    /**
     * Should we display all the native build output?
     * 
     * @parameter default-value="false"
     */
    private boolean verbose;

    /**
     * Extra arguments you want to pass to the configure command.
     * 
     * @parameter
     */
    private List<Arg> configureArgs;

    private final CLI cli = new CLI();

    public void execute() throws MojoExecutionException {
    	cli.verbose = verbose;
    	cli.log = getLog();
        try {
            File buildDir = new File(buildDirectory, "native-build");
            buildDir.mkdirs();
            FileUtils.copyDirectoryStructureIfModified(packageDirectory, buildDir);
            
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
            throw new MojoExecutionException("make failed: "+e, e);
        } 
    }

    private void vsBasedBuild(File buildDir) throws CommandLineException, MojoExecutionException, IOException {
    	
        Library library = new Library(name);
        String platform;
        String configuration="release";
        if( "windows32".equals(library.getPlatform()) ) {
        	platform = "Win32";
        } else if( "windows64".equals(library.getPlatform()) ) {
        	platform = "x64";
        } else {
        	throw new MojoExecutionException("Usupported platform: "+library.getPlatform());
        }
    	
        //TODO: look into supporting cross compilation 
        int rc = cli.system(buildDir, new String[]{"vcbuild", "/platform:"+platform, "vs2008.vcproj", configuration});
        if( rc != 0 ) {
            throw new MojoExecutionException("vcbuild failed with exit code: "+rc);
        }        
        
        File libFile=FileUtils.resolveFile(buildDir, "target/"+platform+"-"+configuration+"/lib/"+library.getLibraryFileName());
        if( !libFile.exists() ) {
            throw new MojoExecutionException("vcbuild did not generate: "+libFile);
        }        

        File target=FileUtils.resolveFile(libDirectory, library.getPlatformSpecifcResourcePath());
        FileUtils.copyFile(libFile, target);
	}

    
	private void configureBasedBuild(File buildDir) throws IOException, MojoExecutionException, CommandLineException {
        File configure = new File(buildDir, "configure");
        File autogen = new File(buildDir, "autogen.sh");
        File makefile = new File(buildDir, "Makefile");
        
        File distDirectory = new File(buildDir, "target");
        File distLibDirectory = new File(distDirectory, "lib");
		distLibDirectory.mkdirs();
        
        if( autogen.exists() && !skipAutogen ) {
            if( (!configure.exists() && !CLI.IS_WINDOWS) || forceAutogen ) {
                if( !autogen.canExecute() ) {
                    cli.chmod("a+x", autogen);
                }
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
                        if( !file.canExecute() ) {
                            cli.chmod("a+x", file);
                        }
                    }
                }
                
                if( !configure.canExecute() ) {
                    cli.chmod("a+x", configure);
                }
                int rc = cli.system(buildDir, new String[]{"./configure", "--disable-ccache", "--prefix="+distDirectory.getCanonicalPath()}, configureArgs);
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
        File target=FileUtils.resolveFile(libDirectory, library.getPlatformSpecifcResourcePath());
        FileUtils.copyFile(libFile, target);
    }

}
