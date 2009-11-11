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
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.interpolation.InterpolatorFilterReader;
import org.codehaus.plexus.interpolation.MapBasedValueSource;
import org.codehaus.plexus.interpolation.StringSearchInterpolator;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.FileUtils.FilterWrapper;
import org.codehaus.plexus.util.cli.Arg;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.DefaultConsumer;

/**
 * A Maven Mojo that allows you to generate a automake based build package for a
 * JNI module.
 * 
 * @goal package
 * @phase prepare-package
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class GeneratePackageMojo extends AbstractMojo {

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
     * @parameter default-value="${project.build.directory}/native-package"
     */
    private File packageDirectory;
    
    /**
     * The classifier of the package archive that will be created.
     * 
     * @parameter default-value="native-src"
     */
    private String classifier;
    

    /**
     * The directory where the generated native files are located..
     * 
     * @parameter default-value="${project.build.directory}/generated-sources/hawtjni/native"
     */
    private File nativeSrc;

    /**
     * The base name of the library, used to determine generated file names.
     * 
     * @parameter default-value="${project.artifactId}"
     */
    private String name;

    /**
     * The list of additional files to be included in the package will be
     * placed.
     * 
     * @parameter default-value="${basedir}/src/main/native-package"
     */
    private File resources;

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

            if( nativeSrc!=null && nativeSrc.isDirectory() ) {
                FileUtils.copyDirectoryStructure(nativeSrc, targetSrcDir);
            }
            
            if( resources!=null && resources.isDirectory() ) {
                FileUtils.copyDirectoryStructure(resources, packageDirectory);
            }
            
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
            
            String packageName = project.getArtifactId()+"-"+project.getVersion()+"-"+classifier;
            Archiver archiver = archiverManager.getArchiver( "zip" );
            File packageFile = new File(new File(project.getBuild().getDirectory()), packageName+".zip");
            archiver.setDestFile( packageFile);
            archiver.setIncludeEmptyDirs(true);
            archiver.addDirectory(packageDirectory, packageName+"/");
            archiver.createArchive();
            
            projectHelper.attachArtifact( project, "zip", classifier, packageFile );
            
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
                values.put("FIRST_SOURCE_FILE", "src/"+f);
                first=false;
            }
            sources += "  src/"+f;
            
            xml_sources+="      <File RelativePath=\"src/"+ (f.replace("/", "\\")) +"\"/>\n";
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
        int rc = CommandLineUtils.executeCommandLine(cli, null, new DefaultConsumer(), new DefaultConsumer());
        getLog().info("rc: "+rc);
        return rc;
    }    
}
