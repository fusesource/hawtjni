package org.fusesource.hawtjni.ui;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOSupport {

    static public String loadFile(File f) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileInputStream is = null;
        try {
            is = new FileInputStream(f);
            transfer(is, baos);
            return new String(baos.toByteArray());
        } catch (IOException e) {
            return null;
        } finally {
            close(is);
        }
    }

    static public void transfer(InputStream is, File file) throws IOException {
        FileOutputStream os = new FileOutputStream(file);
        try {
            transfer(is, os);
        } finally {
            close(is);
            close(os);
        }
    }
    
    public static void transfer(InputStream is, OutputStream os) throws IOException {
        byte buffer[] = new byte[1024*4];
        int c;
        while( (c=is.read(buffer, 0, buffer.length))>=0  ) {
            os.write(buffer, 0, c);
        }
    }

    public static void close(FileOutputStream os) {
        try {
            os.close();
        } catch (Throwable e) {
        }
    }

    public static void close(InputStream is) {
        try {
            is.close();
        } catch (Throwable e) {
        }
    }
}
