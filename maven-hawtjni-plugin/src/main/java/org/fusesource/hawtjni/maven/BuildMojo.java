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
import java.util.regex.Pattern;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.Arg;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.codehaus.plexus.util.cli.CommandLineUtils.StringStreamConsumer;
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

    public void execute() throws MojoExecutionException {
        try {
            File pd = new File(buildDirectory, "native-build");
            if ( isWindows() ) {
                throw new MojoExecutionException("Windows builds not supported yet.");
            } else {
                configureBasedBuild(pd);
            }
            
            getLog().info("Adding test resource root: "+libDirectory.getAbsolutePath());
            Resource testResource = new Resource();
            testResource.setDirectory(libDirectory.getAbsolutePath());
            this.project.addTestResource(testResource); //();
            
        } catch (Exception e) {
            throw new MojoExecutionException("make failed: "+e, e);
        } 
    }

    private void configureBasedBuild(File buildDir) throws IOException, MojoExecutionException, CommandLineException {
        File distDirectory = new File(buildDirectory, "native-dist");
        File configure = new File(buildDir, "configure");
        File autogen = new File(buildDir, "autogen.sh");
        File makefile = new File(buildDir, "Makefile");

        new File(distDirectory, "lib").mkdirs();
        FileUtils.copyDirectoryStructure(packageDirectory, buildDir);
        
        if( autogen.exists() && !skipAutogen ) {
            if( !autogen.exists() ) {
                throw new MojoExecutionException("The autogen file does not exist: "+autogen);
            }
            if( !configure.exists() || forceAutogen ) {
                chmod("a+x", autogen);
                int rc = system(buildDir, new String[] {"./autogen.sh"}, autogenArgs);
                if( rc != 0 ) {
                    throw new MojoExecutionException("./autogen.sh failed with exit code: "+rc);
                }
            }
        }
        
        if( configure.exists() && !skipConfigure ) {
            if( !configure.exists() ) {
                throw new MojoExecutionException("The configure file does not exist: "+configure);
            }
            if( !makefile.exists() || forceConfigure ) {
                chmod("a+x", configure);
                int rc = system(buildDir, new String[]{"./configure", "--disable-ccache", "--prefix="+distDirectory.getCanonicalPath()}, configureArgs);
                if( rc != 0 ) {
                    throw new MojoExecutionException("./configure failed with exit code: "+rc);
                }
            }
        }
        
        
        File autotools = new File(buildDir, "autotools");
        File[] listFiles = autotools.listFiles();
        if( listFiles!=null ) {
            for (File file : listFiles) {
                chmod("a+x", file);
            }
        }
        
        int rc = system(buildDir, new String[]{"make", "install"});
        if( rc != 0 ) {
            throw new MojoExecutionException("make based build failed with exit code: "+rc);
        }
        
        Library library = new Library(name);
        
        File libFile = new File(new File(distDirectory, "lib"), library.getLibraryFileName());
        if( !libFile.exists() ) {
            throw new MojoExecutionException("Make based build did not generate: "+libFile);
        }
        File target=FileUtils.resolveFile(libDirectory, library.getPlatformSpecifcResorucePath());
        FileUtils.copyFile(libFile, target);
    }

    private boolean isWindows() {
        String name = System.getProperty("os.name").toLowerCase().trim();
        return name.startsWith("win");
    }

    private void chmod(String permision, File path) {
        if( !path.canExecute() ) {
            try {
                system(path.getParentFile(), new String[] { "chmod", permision, path.getCanonicalPath() });
            } catch (Throwable e) {
            }
        }
    }
    
    private int system(File wd, String[] command) throws CommandLineException {
        return system(wd, command, null);
    }
    
    private int system(File wd, String[] command, List<Arg> args) throws CommandLineException {
        Commandline cli = new Commandline();
        cli.setWorkingDirectory(wd);
        for (String c : command) {
            cli.createArg().setValue(c);
        }
        if( args!=null ) {
            for (Arg arg : args) {
                cli.addArg(arg);
            }
        }
        getLog().info("executing: "+cli);
        
        StreamConsumer consumer = new StreamConsumer() {
            public void consumeLine(String line) {
                getLog().info(line);
            }
        };
        if( !verbose ) {
            consumer = new StringStreamConsumer();
        }
        int rc = CommandLineUtils.executeCommandLine(cli, null, consumer, consumer);
        if( rc!=0 ) {
            if( !verbose ) {
                // We only display output if the command fails..
                String output = ((StringStreamConsumer)consumer).getOutput();
                if( output.length()>0 ) {
                    String nl = System.getProperty( "line.separator");
                    String[] lines = output.split(Pattern.quote(nl));
                    for (String line : lines) {
                        getLog().info(line);
                    }
                }
            }
            getLog().info("rc: "+rc);
        } else {
            if( !verbose ) {
                String output = ((StringStreamConsumer)consumer).getOutput();
                if( output.length()>0 ) {
                    String nl = System.getProperty( "line.separator");
                    String[] lines = output.split(Pattern.quote(nl));
                    for (String line : lines) {
                        getLog().debug(line);
                    }
                }
            }
            getLog().debug("rc: "+rc);
        }
        return rc;
    }
}
