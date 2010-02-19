package org.fusesource.hawtjni.maven;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.codehaus.plexus.util.cli.CommandLineUtils.StringStreamConsumer;

public class CLI {
	
	public static final boolean IS_WINDOWS = isWindows();
	
    static private boolean isWindows() {
        String name = System.getProperty("os.name").toLowerCase().trim();
        return name.startsWith("win");
    }
   
	public boolean verbose;
	public Log log;

	
    public void setExecutable(File path) {
        if( IS_WINDOWS ) {
            return;
        }
        try {
            // These are Java 1.6 Methods..
            if( !path.canExecute() ) {
                path.setExecutable(true);
            }
        } catch (NoSuchMethodError e1) {
            // Do it the old fasioned way...
            try {
                system(path.getParentFile(), new String[] { "chmod", "a+x", path.getCanonicalPath() });
            } catch (Throwable e2) {
            }
        }
    }
    
    public int system(File wd, String[] command) throws CommandLineException {
        return system(wd, command, null);
    }
    
    public int system(File wd, String[] command, List<String> args) throws CommandLineException {
        Commandline cli = new Commandline();
        cli.setWorkingDirectory(wd);
        for (String c : command) {
            cli.createArg().setValue(c);
        }
        if( args!=null ) {
            for (String arg : args) {
                cli.createArg().setValue(arg);
            }
        }
        log.info("executing: "+cli);
        
        StreamConsumer consumer = new StreamConsumer() {
            public void consumeLine(String line) {
                log.info(line);
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
                        log.info(line);
                    }
                }
            }
            log.info("rc: "+rc);
        } else {
            if( !verbose ) {
                String output = ((StringStreamConsumer)consumer).getOutput();
                if( output.length()>0 ) {
                    String nl = System.getProperty( "line.separator");
                    String[] lines = output.split(Pattern.quote(nl));
                    for (String line : lines) {
                        log.debug(line);
                    }
                }
            }
            log.debug("rc: "+rc);
        }
        return rc;
    }
    
}
