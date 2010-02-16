/**
 * 
 */
package org.fusesource.hawtjni.clang;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fusesource.hawtjni.clang.jaxb.ClangXml;

public class ClangSupport {

    static public ClangXml load(String header, List<String> defines) throws IOException, InterruptedException, JAXBException, XMLStreamException {

        File workDir = File.createTempFile("hawtjni-ui", "tmp");
        workDir.delete();
        workDir.mkdirs();
        File cFile = new File(workDir, "temp.c");
        PrintWriter w = new PrintWriter(new FileWriter(cFile));
        try {
            w.println("#include <" + header + ">");
        } finally {
            w.close();
        }

        ArrayList<String> command = new ArrayList<String>();
        command.add("clang");
        command.add("-fsyntax-only");
        command.add("-Xclang");
        command.add("--ast-print-xml");
        if (defines != null) {
            for (String def : defines) {
                command.add("-D" + def);
            }
        }
        command.add("temp.c");
        String args[] = command.toArray(new String[command.size()]);
        Process process = Runtime.getRuntime().exec(args, null, workDir);
        process(process.getErrorStream(), System.out);
        process(process.getInputStream(), System.out);
        if (process.waitFor() != 0) {
            throw new IOException("clang failed.");
        }

        File xmlFile = new File(workDir, "temp.xml");
        FileInputStream is = new FileInputStream(xmlFile);
        try {
            return load(is);
        } finally {
            try {
                is.close();
            } catch (Throwable e) {
            }
        }
    }

    private static void process(final InputStream is, final OutputStream os) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                byte buffer[] = new byte[1024*4];
                int c;
                try {
                    while( (c=is.read(buffer, 0, buffer.length)) > 0 ) {
                        os.write(buffer, 0, c);
                    }
                } catch (IOException e) {
                }
            }
        };
        thread.setDaemon(true);
        thread.start();
    }

    static public ClangXml load(InputStream is) throws JAXBException, XMLStreamException {
        JAXBContext context = JAXBContext.newInstance("org.fusesource.hawtjni.clang.jaxb");
        Unmarshaller unmarshaller = context.createUnmarshaller();

        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader reader = factory.createXMLStreamReader(is);
        ClangXml rc = (ClangXml) unmarshaller.unmarshal(reader);
        rc.index();
        return rc;
    }

}