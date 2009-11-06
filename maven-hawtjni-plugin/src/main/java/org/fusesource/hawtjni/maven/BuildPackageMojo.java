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
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.Arg;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.DefaultConsumer;

/**
 * A Maven Mojo that allows you to build an automake based package.
 * 
 * @goal build
 * @phase compile
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class BuildPackageMojo extends AbstractMojo {

    /**
     * Where the package is located.
     * 
     * @parameter default-value="${basedir}/src/main/native-package"
     */
    private File sourceDirectory;

    /**
     * The directory where the build will be produced.  It creates a native-src and native-dist directory
     * under this setting.
     * 
     * @parameter default-value="${project.build.directory}"
     */
    private File targetDirectory;

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
     * Extra arguments you want to pass to the configure command.
     * 
     * @parameter
     */
    private List<Arg> configureArgs;

    public void execute() throws MojoExecutionException {
        try {
            
            File packageDirectory = new File(targetDirectory, "native-src");
            File distDirectory = new File(targetDirectory, "native-dist");
            File configure = new File(packageDirectory, "configure");
            File autogen = new File(packageDirectory, "autogen.sh");
            File makefile = new File(packageDirectory, "Makefile");

            packageDirectory.mkdirs();
            distDirectory.mkdirs();
            FileUtils.copyDirectoryStructure(sourceDirectory, packageDirectory);
            
            
            if( autogen.exists() && !skipAutogen ) {
                if( !autogen.exists() ) {
                    throw new MojoExecutionException("The autogen file does not exist: "+autogen);
                }
                if( !configure.exists() || forceAutogen ) {
                    chmod("a+x", autogen);
                    system(packageDirectory, new String[] {"./autogen.sh"}, autogenArgs);
                }
            }
            
            if( configure.exists() && !skipConfigure ) {
                if( !configure.exists() ) {
                    throw new MojoExecutionException("The configure file does not exist: "+configure);
                }
                if( !makefile.exists() || forceConfigure ) {
                    chmod("a+x", configure);
                    system(packageDirectory, new String[]{"./configure", "--disable-ccache", "--prefix="+distDirectory.getCanonicalPath()}, configureArgs);
                }
            }
            system(packageDirectory, new String[]{"make", "install"});
            
        } catch (Exception e) {
            throw new MojoExecutionException("make failed: "+e, e);
        } 
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
        int rc = CommandLineUtils.executeCommandLine(cli, null, new DefaultConsumer(), new DefaultConsumer());
        getLog().info("rc: "+rc);
        return rc;
    }
}
