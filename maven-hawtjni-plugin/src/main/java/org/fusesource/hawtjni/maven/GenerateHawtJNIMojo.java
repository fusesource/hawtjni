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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.fusesource.hawtjni.generator.HawtJNI;
import org.fusesource.hawtjni.generator.ProgressMonitor;

/**
 * A Maven Mojo that allows you to generate JNI code using HawtJNI.
 * 
 * @goal generate
 * @phase process-classes
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class GenerateHawtJNIMojo extends AbstractMojo {

    /**
     * The maven project.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * The directory where the generated native files will be placed.
     * 
     * @parameter default-value=
     *            "${project.build.directory}/generated-sources/hawtjni/native"
     */
    private File nativeOutput;

    /**
     * The base name of the library, used to determine generated file names.
     * 
     * @parameter default-value="${project.artifactId}"
     */
    private String name;

    /**
     * The copyright header template that will be added to the generated source files.
     * Use the '%END_YEAR%' token to have it replaced with the current year.  
     * 
     * @parameter default-value=""
     */
    private String copyright;

    /**
     * Restrict looking for JNI classes to the specified package.
     *  
     * @parameter
     */
    private List<String> packages = new ArrayList<String>();

//    /**
//     * The directory where the generated java files will be paced.
//     * 
//     * @parameter default-value=
//     *            "${project.build.directory}/generated-sources/hawtjni/java"
//     */
//    private File javaOutput;
//
    /**
     * The directory where the java classes files are located.
     * 
     * @parameter default-value="${project.build.outputDirectory}"
     */
    private File classesDirectory;

    public void execute() throws MojoExecutionException {

        HawtJNI generator = new HawtJNI();
        generator.setClasspaths(getClasspath());
        generator.setName(name);
        generator.setCopyright(copyright);
        generator.setNativeOutput(nativeOutput);
        generator.setPackages(packages);
        generator.setProgress(new ProgressMonitor() {
            public void step() {
            }
            public void setTotal(int total) {
            }
            public void setMessage(String message) {
                getLog().info(message);
            }
        });
        try {
            generator.generate();
        } catch (Exception e) {
            throw new MojoExecutionException("Failed: "+e, e);
        }
    }

    @SuppressWarnings("unchecked")
    private ArrayList<String> getClasspath() throws MojoExecutionException {
        ArrayList<String> artifacts = new ArrayList<String>();
        try {
            artifacts.add(classesDirectory.getCanonicalPath());
            for (Artifact artifact : (Set<Artifact>) project.getArtifacts()) {
                File file = artifact.getFile();
                    getLog().info("Including: " + file);
                    artifacts.add(file.getCanonicalPath());
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Could not determine project classath.", e);
        }
        return artifacts;
    }

}
