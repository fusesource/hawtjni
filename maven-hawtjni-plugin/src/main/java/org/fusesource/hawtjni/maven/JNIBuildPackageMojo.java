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

/**
 * A Maven Mojo that allows you to generate a automake based build package for a
 * JNI module.
 * 
 * @goal build-package
 * @phase package
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class JNIBuildPackageMojo extends AbstractMojo {

    /**
     * The maven project.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

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
     * The directory where the generated native files are located..
     * 
     * @parameter default-value="${project.build.directory}/generated-sources/hawtjni/native-package"
     */
    private File packageDirectory;
    
    /**
     * The classifier of the package archive that will be created.
     * 
     * @parameter default-value="native-src"
     */
    private String sourceClassifier;
    
    public void execute() throws MojoExecutionException {
        try {
            String packageName = project.getArtifactId()+"-"+project.getVersion()+"-"+sourceClassifier;
            Archiver archiver = archiverManager.getArchiver( "zip" );
            File packageFile = new File(new File(project.getBuild().getDirectory()), packageName+".zip");
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
