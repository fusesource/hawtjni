/*******************************************************************************
 * Copyright (C) 2009-2011 FuseSource Corp.
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.fusesource.hawtjni.generator.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class FileSupport {

    public static boolean write(byte[] bytes, File file) throws IOException {
        if( !equals(bytes, file) ) {
            FileOutputStream out = new FileOutputStream(file);
            try {
                out.write(bytes);
            } finally {
                out.close();
            }
            return true;
        }
        return false;
    }

    public static void copy(InputStream is, OutputStream os) throws IOException {
        try {
            byte data[] = new byte[1024*4];
            int count;
            while( (count=is.read(data, 0, data.length))>=0 ) {
                os.write(data, 0, count);
            }
        } finally {
            close(is);
            close(os);
        }
    }

    public static boolean equals(byte[] bytes, File file) throws IOException {
        FileInputStream is = null;
        try {
            is = new FileInputStream(file);
            return equals(new ByteArrayInputStream(bytes), new BufferedInputStream(is));
        } catch (FileNotFoundException e) {
            return false;
        } finally {
            close(is);
        }
    }

    public static void close(InputStream is) {
        try {
            if (is != null)
                is.close();
        } catch (Throwable e) {
        }
    }
    
    public static void close(OutputStream ioss) {
        try {
            if (ioss != null)
                ioss.close();
        } catch (Throwable e) {
        }
    }
    
    public static boolean equals(InputStream is1, InputStream is2) throws IOException {
        while (true) {
            int c1 = is1.read();
            int c2 = is2.read();
            if (c1 != c2)
                return false;
            if (c1 == -1)
                break;
        }
        return true;
    }

    

}
