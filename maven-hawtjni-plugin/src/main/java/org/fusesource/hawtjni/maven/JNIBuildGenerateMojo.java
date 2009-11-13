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
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.interpolation.InterpolatorFilterReader;
import org.codehaus.plexus.interpolation.MapBasedValueSource;
import org.codehaus.plexus.interpolation.StringSearchInterpolator;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.FileUtils.FilterWrapper;
import org.codehaus.plexus.util.cli.Arg;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.codehaus.plexus.util.cli.CommandLineUtils.StringStreamConsumer;

/**
 * A Maven Mojo that allows you to generate a automake based build package for a
 * JNI module.
 * 
 * @goal build-generate
 * @phase process-classes
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class JNIBuildGenerateMojo extends AbstractMojo {

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
     * The directory where the generated build package is located..
     * 
     * @parameter default-value="${project.build.directory}/generated-sources/hawtjni/native-package"
     */
    private File packageDirectory;
    
    /**
     * The directory where the generated native source files are located.
     * 
     * @parameter default-value="${project.build.directory}/generated-sources/hawtjni/native-src"
     */
    private File generatedNativeSourceDirectory;

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
     * Should we display all the native build output?
     * 
     * @parameter default-value="false"
     */
    private boolean verbose;

    /**
     * Extra arguments you want to pass to the autogen.sh command.
     * 
     * @parameter
     */
    private List<Arg> autogenArgs;
    
    private File targetSrcDir;

    public void execute() throws MojoExecutionException {
        
        try {
            
            FileUtils.deleteDirectory(packageDirectory);
            packageDirectory.mkdirs();
            new File(packageDirectory, "m4").mkdirs();
            targetSrcDir = new File(packageDirectory, "src");
            targetSrcDir.mkdirs();

            if( generatedNativeSourceDirectory!=null && generatedNativeSourceDirectory.isDirectory() ) {
                FileUtils.copyDirectoryStructure(generatedNativeSourceDirectory, targetSrcDir);
            }
            
            if( customPackageDirectory!=null && customPackageDirectory.isDirectory() ) {
                FileUtils.copyDirectoryStructure(customPackageDirectory, packageDirectory);
            }
            
            copyTemplateResource("readme.md", false);
            copyTemplateResource("configure.ac", true);
            copyTemplateResource("Makefile.am", true);
            copyTemplateResource("autogen.sh", false);
            copyTemplateResource("m4/jni.m4", false);
            copyTemplateResource("m4/osx-universal.m4", false);

            // To support windows based builds..
            copyTemplateResource("vs2008.vcproj", true);
            
            File configure = new File(packageDirectory, "configure");
            File autogen = new File(packageDirectory, "autogen.sh");
            if( autogen.exists() && !skipAutogen ) {
                if( !autogen.exists() ) {
                    throw new MojoExecutionException("The autogen file does not exist: "+autogen);
                }
                if( !configure.exists() || forceAutogen ) {
                    chmod("a+x", autogen);
                    system(packageDirectory, new String[] {"./autogen.sh"}, autogenArgs);
                }
            }
            
        } catch (Exception e) {
            throw new MojoExecutionException("packageing failed: "+e, e);
        } 
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
        
        List<String> files = new ArrayList<String>();
        files.addAll(FileUtils.getFileNames(targetSrcDir, "**/*.c", null, false));
        files.addAll(FileUtils.getFileNames(targetSrcDir, "**/*.cpp", null, false));
        files.addAll(FileUtils.getFileNames(targetSrcDir, "**/*.cxx", null, false));
        String sources = "";
        String xml_sources = "";
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
        }
        
        values.put("PROJECT_SOURCES", sources);
        values.put("PROJECT_XML_SOURCES", xml_sources);

        
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
