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
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.fusesource.hawtjni.runtime.Library;

/**
 * This goal allows allows you to package the JNI library created by build goal
 * in a JAR which the HawtJNI runtime can unpack when the library needs to be loaded.
 * 
 * This platform specific jar is attached with a classifier which matches the current 
 * platform.
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
     * The classifier of the jar will use.  If not specified it will be set to the platform
     * string determined by the HawtJNI Library class.
     * 
     * @parameter
     */
    private String jarClassifier;
    
    public void execute() throws MojoExecutionException {
        try {
            
            if( jarClassifier == null ) {
                Library library = new Library(name);
                jarClassifier = library.getPlatform();
            }
            
            String packageName = project.getArtifactId()+"-"+project.getVersion()+"-"+jarClassifier;
            Archiver archiver = archiverManager.getArchiver( "jar" );
            
            File packageFile = new File(new File(project.getBuild().getDirectory()), packageName+".jar");
            archiver.setDestFile( packageFile);
            archiver.setIncludeEmptyDirs(true);
            archiver.addDirectory(libDirectory);
            archiver.createArchive();
            
            projectHelper.attachArtifact( project, "jar", jarClassifier, packageFile );
            
        } catch (Exception e) {
            throw new MojoExecutionException("packageing failed: "+e, e);
        } 
    }

}
