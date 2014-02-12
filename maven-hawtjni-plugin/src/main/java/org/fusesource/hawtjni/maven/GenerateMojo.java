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
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.interpolation.InterpolatorFilterReader;
import org.codehaus.plexus.interpolation.MapBasedValueSource;
import org.codehaus.plexus.interpolation.StringSearchInterpolator;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.FileUtils.FilterWrapper;
import org.fusesource.hawtjni.generator.HawtJNI;
import org.fusesource.hawtjni.generator.ProgressMonitor;

/**
 * This goal generates the native source code and a
 * autoconf/msbuild based build system needed to 
 * build a JNI library for any HawtJNI annotated
 * classes in your maven project.
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
     * The directory where the native source files are located.
     *
     * @parameter
     */
    private File nativeSourceDirectory;

    /**
     * The directory where the generated native source files are located.
     * 
     * @parameter default-value="${project.build.directory}/generated-sources/hawtjni/native-src"
     */
    private File generatedNativeSourceDirectory;

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

    /**
     * The directory where the java classes files are located.
     * 
     * @parameter default-value="${project.build.outputDirectory}"
     */
    private File classesDirectory;
    
    /**
     * The directory where the generated build package is located..
     * 
     * @parameter default-value="${project.build.directory}/generated-sources/hawtjni/native-package"
     */
    private File packageDirectory;
    
    /**
     * The list of additional files to be included in the package will be
     * placed.
     * 
     * @parameter default-value="${basedir}/src/main/native-package"
     */
    private File customPackageDirectory;

    /**
     * The text encoding of the files.
     * 
     * @parameter default-value="UTF-8"
     */
    private String encoding;

    /**
     * Should we skip executing the autogen.sh file.
     * 
     * @parameter default-value="${skip-autogen}"
     */
    private boolean skipAutogen;
    
    /**
     * Should we force executing the autogen.sh file.
     * 
     * @parameter default-value="${force-autogen}"
     */
    private boolean forceAutogen;

    /**
     * Should we display all the native build output?
     * 
     * @parameter default-value="${hawtjni-verbose}"
     */
    private boolean verbose;

    /**
     * Extra arguments you want to pass to the autogen.sh command.
     * 
     * @parameter
     */
    private List<String> autogenArgs;
    
    /**
     * Set this value to false to disable the callback support in HawtJNI.
     * Disabling callback support can substantially reduce the size
     * of the generated native library.  
     * 
     * @parameter default-value="true"
     */
    private boolean callbacks;
    
    /**
     * The build tool to use on Windows systems.  Set
     * to 'msbuild', 'vcbuild', or 'detect'
     *
     * @parameter default-value="detect"
     */
    private String windowsBuildTool;

    private File targetSrcDir;
    
    private CLI cli = new CLI();

    public void execute() throws MojoExecutionException {
    	cli.verbose = verbose;
    	cli.log = getLog();
        if (nativeSourceDirectory == null) {
            generateNativeSourceFiles();
        } else {
            copyNativeSourceFiles();
        }
        generateBuildSystem();
    }

    private void copyNativeSourceFiles() throws MojoExecutionException {
        try {
            FileUtils.copyDirectory(nativeSourceDirectory, generatedNativeSourceDirectory);
        } catch (Exception e) {
            throw new MojoExecutionException("Copy of Native source failed: "+e, e);
        }
    }

    private void generateNativeSourceFiles() throws MojoExecutionException {
        HawtJNI generator = new HawtJNI();
        generator.setClasspaths(getClasspath());
        generator.setName(name);
        generator.setCopyright(copyright);
        generator.setNativeOutput(generatedNativeSourceDirectory);
        generator.setPackages(packages);
        generator.setCallbacks(callbacks);
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
            throw new MojoExecutionException("Native source code generation failed: "+e, e);
        }
    }

    private void generateBuildSystem() throws MojoExecutionException {
        try {
            packageDirectory.mkdirs();
            new File(packageDirectory, "m4").mkdirs();
            targetSrcDir = new File(packageDirectory, "src");
            targetSrcDir.mkdirs();

            if( customPackageDirectory!=null && customPackageDirectory.isDirectory() ) {
                FileUtils.copyDirectoryStructureIfModified(customPackageDirectory, packageDirectory);
            }

            if( generatedNativeSourceDirectory!=null && generatedNativeSourceDirectory.isDirectory() ) {
                FileUtils.copyDirectoryStructureIfModified(generatedNativeSourceDirectory, targetSrcDir);
            }
            
            copyTemplateResource("readme.md", false);
            copyTemplateResource("configure.ac", true);
            copyTemplateResource("Makefile.am", true);
            copyTemplateResource("m4/custom.m4", false);
            copyTemplateResource("m4/jni.m4", false);
            copyTemplateResource("m4/osx-universal.m4", false);

            // To support windows based builds..
            String tool = windowsBuildTool.toLowerCase().trim();
            if( "detect".equals(tool) ) {
                copyTemplateResource("vs2008.vcproj", true);
                copyTemplateResource("vs2010.vcxproj", true);
            } else if( "msbuild".equals(tool) ) {
                copyTemplateResource("vs2010.vcxproj", true);
            } else if( "vcbuild".equals(tool) ) {
                copyTemplateResource("vs2008.vcproj", true);
            } else if( "none".equals(tool) ) {
            } else {
                throw new MojoExecutionException("Invalid setting for windowsBuildTool: "+windowsBuildTool);
            }

            File autogen = new File(packageDirectory, "autogen.sh");
            File configure = new File(packageDirectory, "configure");
            if( !autogen.exists() ) {
                copyTemplateResource("autogen.sh", false);
                cli.setExecutable(autogen);
            }
            if( !skipAutogen ) {
                if( (!configure.exists() && !CLI.IS_WINDOWS) || forceAutogen ) {
                    try {
                        cli.system(packageDirectory, new String[] {"./autogen.sh"}, autogenArgs);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            
            
        } catch (Exception e) {
            throw new MojoExecutionException("Native build system generation failed: "+e, e);
        }
    }

    @SuppressWarnings("unchecked")
    private ArrayList<String> getClasspath() throws MojoExecutionException {
        ArrayList<String> artifacts = new ArrayList<String>();
        try {
            artifacts.add(classesDirectory.getCanonicalPath());
            for (Artifact artifact : (Set<Artifact>) project.getArtifacts()) {
                File file = artifact.getFile();
                getLog().debug("Including: " + file);
                artifacts.add(file.getCanonicalPath());
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Could not determine project classath.", e);
        }
        return artifacts;
    }

    private void copyTemplateResource(String file, boolean filter) throws MojoExecutionException {
        try {
            File target = FileUtils.resolveFile(packageDirectory, file);
            if( target.isFile() && target.canRead() ) {
                return;
            }
            URL source = getClass().getClassLoader().getResource("project-template/" + file);
            File tmp = FileUtils.createTempFile("tmp", "txt", new File(project.getBuild().getDirectory()));
            try {
                FileUtils.copyURLToFile(source, tmp);
                FileUtils.copyFile(tmp, target, encoding, filters(filter), true);
            } finally {
                tmp.delete();
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Could not extract template resource: "+file, e);
        }
    }

    @SuppressWarnings("unchecked")
    private FilterWrapper[] filters(boolean filter) throws IOException {
        if( !filter ) {
            return new FilterWrapper[0];
        }

        final String startExp = "@";
        final String endExp = "@";
        final String escapeString = "\\";
        final Map<String,String> values = new HashMap<String,String>();
        values.put("PROJECT_NAME", name);
        values.put("PROJECT_NAME_UNDER_SCORE", name.replaceAll("\\W", "_"));
        values.put("VERSION", project.getVersion());
        
        List<String> cpp_files = new ArrayList<String>();
        cpp_files.addAll(FileUtils.getFileNames(targetSrcDir, "**/*.cpp", null, false));
        cpp_files.addAll(FileUtils.getFileNames(targetSrcDir, "**/*.cxx", null, false));

        List<String> files = new ArrayList<String>();
        files.addAll(cpp_files);
        files.addAll(FileUtils.getFileNames(targetSrcDir, "**/*.c", null, false));
        files.addAll(FileUtils.getFileNames(targetSrcDir, "**/*.m", null, false));
        String sources = "";
        String xml_sources = "";
        String vs10_sources = "";
        boolean first = true;
        for (String f : files) {
            if( !first ) {
                sources += "\\\n";
            } else {
                values.put("FIRST_SOURCE_FILE", "src/"+f.replace('\\', '/'));
                first=false;
            }
            sources += "  src/"+f;
            
            xml_sources+="      <File RelativePath=\".\\src\\"+ (f.replace('/', '\\')) +"\"/>\n";
            vs10_sources+="    <ClCompile Include=\".\\src\\"+ (f.replace('/', '\\')) +"\"/>\n";
        }

        if( cpp_files.isEmpty() ) {
            values.put("AC_PROG_CHECKS", "AC_PROG_CC");
        } else {
            values.put("AC_PROG_CHECKS", "AC_PROG_CXX");
        }

        values.put("PROJECT_SOURCES", sources);
        values.put("PROJECT_XML_SOURCES", xml_sources);
        values.put("PROJECT_VS10_SOURCES", vs10_sources);

        FileUtils.FilterWrapper wrapper = new FileUtils.FilterWrapper() {
            public Reader getReader(Reader reader) {
                StringSearchInterpolator propertiesInterpolator = new StringSearchInterpolator(startExp, endExp);
                propertiesInterpolator.addValueSource(new MapBasedValueSource(values));
                propertiesInterpolator.setEscapeString(escapeString);
                InterpolatorFilterReader interpolatorFilterReader = new InterpolatorFilterReader(reader, propertiesInterpolator, startExp, endExp);
                interpolatorFilterReader.setInterpolateWithPrefixPattern(false);
                return interpolatorFilterReader;
            }
        };
        return new FilterWrapper[] { wrapper };
    }
    

}
