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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilder;
import org.fusesource.hawtjni.generator.JNIGeneratorApp;

/**
 * A Maven Mojo that allows you to generate JNI code using HawtJNI.
 * 
 * @goal generate
 * @phase process-classes
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class GenerateMojo extends AbstractMojo {

    /**
     * The maven project.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * The directory where the java classe files are located.
     * 
     * @parameter default-value="${project.build.outputDirectory}"
     */
    private File classesDirectory;

    /**
     * The directory where the generated native files will be paced.
     * 
     * @parameter default-value=
     *            "${project.build.directory}/generated-sources/hawtjni/native"
     */
    private File nativeOutputDirectory;

    /**
     * The directory where the generated java files will be paced.
     * 
     * @parameter default-value=
     *            "${project.build.directory}/generated-sources/hawtjni/java"
     */
    private File javaOutputDirectory;

    public void execute() throws MojoExecutionException {

        ArrayList<File> artifacts = new ArrayList<File>();
        for (Artifact artifact : (Set<Artifact>) project.getArtifacts()) {
            File file = artifact.getFile();
            getLog().info("Including: " + file);
            artifacts.add(file);
        }
        
        JNIGeneratorApp generator = new JNIGeneratorApp();
        //TODO:
    }

}
