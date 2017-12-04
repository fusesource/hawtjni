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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;

/**
 * This goal creates a source zip file of the native build
 * module and attaches it to the build so that it can get 
 * deployed.
 * 
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
@Mojo(name = "package-source", defaultPhase = LifecyclePhase.PACKAGE)
public class PackageSourceMojo extends AbstractMojo {

    /**
     * The maven project.
     */
    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject project;

    /**
     */
    @Component
    private ArchiverManager archiverManager;
    
    /**
     */
    @Component
    private MavenProjectHelper projectHelper;    
    
    /**
     * The directory where the generated native files are located..
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-sources/hawtjni/native-package")
    private File packageDirectory;
    
    /**
     * The classifier of the package archive that will be created.
     */
    @Parameter(defaultValue = "native-src")
    private String sourceClassifier;
    
    /**
     * Should we skip executing the autogen.sh file.
     */
    @Parameter(defaultValue = "${skip-autogen}")
    private boolean skipAutogen;
    
    
    public void execute() throws MojoExecutionException {
        try {

            String packageName = project.getArtifactId()+"-"+project.getVersion()+"-"+sourceClassifier;
            File packageFile = new File(new File(project.getBuild().getDirectory()), packageName+".zip");

            // Verify the the configure script got generated before packaging.
            File configure = new File(packageDirectory, "configure");
            if( !skipAutogen && !configure.exists() ) {
                // Looks like this platform could not generate the 
                // configure script.  So don't install deploy
                // partially created source package.
                getLog().info("");
                getLog().warn("Will NOT package the native sources to: "+packageFile);
                getLog().info("  Native source build directory did not contain a 'configure' script.");
                getLog().info("  To ignore this warning and package it up anyways, configure the plugin with: <skipAutogen>true</skipAutogen>");
                getLog().info("");
                return;
            }        
            
            Archiver archiver = archiverManager.getArchiver( "zip" );
            archiver.setDestFile( packageFile);
            archiver.setIncludeEmptyDirs(true);
            archiver.addDirectory(packageDirectory, packageName+"/");
            archiver.createArchive();
            projectHelper.attachArtifact( project, "zip", sourceClassifier, packageFile );
            
        } catch (Exception e) {
            throw new MojoExecutionException("packageing failed: "+e, e);
        } 
    }

}
