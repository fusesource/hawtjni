/*******************************************************************************
 * Copyright (c) 2009 Progress Software, Inc.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package test;

import static org.fusesource.hawtjni.runtime.ArgFlag.CRITICAL;
import static org.fusesource.hawtjni.runtime.ArgFlag.NO_IN;
import static org.fusesource.hawtjni.runtime.ArgFlag.NO_OUT;
import static org.fusesource.hawtjni.runtime.Pointer.FALSE;

import org.fusesource.hawtjni.runtime.ClassFlag;
import org.fusesource.hawtjni.runtime.JniArg;
import org.fusesource.hawtjni.runtime.JniClass;
import org.fusesource.hawtjni.runtime.JniField;
import org.fusesource.hawtjni.runtime.JniMethod;
import org.fusesource.hawtjni.runtime.Library;

/**
 * 
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
@JniClass
public class Platform {
    
    private static Library library = new Library("hawtjni-example", Platform.class);    
	static {
        library.load();
	}

    public static final void main(String args[]) {
        int COUNT = 10;
        
        // We track memory pointers with longs.
        long []ptrArray = new long[COUNT];
        
        long last=0;
        for( int i=0; i < COUNT; i++ ) {
            // Allocate heap space of the structure..
            ptrArray[i] = malloc(foo.SIZEOF);

            // Configure some data for a structure...
            foo f = new foo();
            f.a = i;
            f.b = 1;
            
            byte[] src = "hello world".getBytes();
            System.arraycopy(src, 0, f.c, 0, src.length);
            
            f.c5 = 0;
            f.prev = last;
            
            // Copy the data values into the allocated space.
            memmove(ptrArray[i], f, foo.SIZEOF);
            
            last = ptrArray[i];
        }
        
        // Display a couple of structures...
        print_foo(ptrArray[0]);
        print_foo(ptrArray[1]);
        
        // Pass a pointer array as an argument.. 
        long rc = foowork(ptrArray, COUNT);
        System.out.println("foowork result: "+rc);
        
        for( int i=0; i < COUNT; i++ ) {
            // free up allocated memory.
            free(ptrArray[i]);
        }
    }

    @JniMethod(cast="void *")
    public static final native long malloc(
            @JniArg(cast="size_t") long size);
    
    public static final native void free(
            @JniArg(cast="void *") long ptr);
    

    public static final native void memmove (
            @JniArg(cast="void *") long dest, 
            @JniArg(cast="const void *") long src, 
            @JniArg(cast="size_t") long size);

    public static final native void memmove (
            @JniArg(cast="void *") long dest, 
            @JniArg(cast="const void *", flags={NO_OUT, CRITICAL}) byte[] src, 
            @JniArg(cast="size_t") long size);

    public static final native void memmove (
            @JniArg(cast="void *") long dest, 
            @JniArg(cast="const void *", flags={NO_OUT, CRITICAL}) char[] src, 
            @JniArg(cast="size_t") long size);

    public static final native void memmove (
            @JniArg(cast="void *") long dest, 
            @JniArg(cast="const void *", flags={NO_OUT, CRITICAL})  short[] src, 
            @JniArg(cast="size_t") long size);

    public static final native void memmove (
            @JniArg(cast="void *") long dest, 
            @JniArg(cast="const void *", flags={NO_OUT, CRITICAL})  int[] src, 
            @JniArg(cast="size_t") long size);

    public static final native void memmove (
            @JniArg(cast="void *") long dest, 
            @JniArg(cast="const void *", flags={NO_OUT, CRITICAL}, pointer=FALSE) long[] src, 
            @JniArg(cast="size_t") long size);

    public static final native void memmove (
            @JniArg(cast="void *") long dest, 
            @JniArg(cast="const void *", flags={NO_OUT, CRITICAL}) float[] src, 
            @JniArg(cast="size_t") long size);

    public static final native void memmove (
            @JniArg(cast="void *") long dest, 
            @JniArg(cast="const void *", flags={NO_OUT, CRITICAL}) double[] src, 
            @JniArg(cast="size_t") long size);

    
    
    public static final native void memmove (
            @JniArg(cast="void *", flags={NO_IN, CRITICAL}) byte[] dest, 
            @JniArg(cast="const void *") long src, 
            @JniArg(cast="size_t") long size);

    public static final native void memmove (
            @JniArg(cast="void *", flags={NO_IN, CRITICAL}) char[] dest, 
            @JniArg(cast="const void *") long src, 
            @JniArg(cast="size_t") long size);

    public static final native void memmove (
            @JniArg(cast="void *", flags={NO_IN, CRITICAL}) short[] dest, 
            @JniArg(cast="const void *") long src, 
            @JniArg(cast="size_t") long size);

    public static final native void memmove (
            @JniArg(cast="void *", flags={NO_IN, CRITICAL}) int[] dest, 
            @JniArg(cast="const void *") long src, 
            @JniArg(cast="size_t") long size);

    public static final native void memmove (
            @JniArg(cast="void *", flags={NO_IN, CRITICAL}, pointer=FALSE) long[] dest, 
            @JniArg(cast="const void *") long src, 
            @JniArg(cast="size_t") long size);
    
    public static final native void memmove (
            @JniArg(cast="void *", flags={NO_IN, CRITICAL}) float[] dest, 
            @JniArg(cast="const void *") long src, 
            @JniArg(cast="size_t") long size);

    public static final native void memmove (
            @JniArg(cast="void *", flags={NO_IN, CRITICAL}) double[] dest, 
            @JniArg(cast="const void *") long src, 
            @JniArg(cast="size_t") long size);

    public static final native void memmove (
            @JniArg(cast="void *", flags={NO_IN, CRITICAL}) byte[] dest, 
            @JniArg(cast="const void *", flags={NO_OUT, CRITICAL})  char[] src, 
            @JniArg(cast="size_t") long size);

    public static final native void memmove (
            @JniArg(cast="void *", flags={NO_IN, CRITICAL}) int[] dest, 
            @JniArg(cast="const void *", flags={NO_OUT, CRITICAL}) byte[] src, 
            @JniArg(cast="size_t") long size);

    @JniMethod(cast="void *")
    public static final native long memset (
            @JniArg(cast="void *") long buffer, 
            int c, 
            @JniArg(cast="size_t") long num);
    
    public static final native int strlen(
            @JniArg(cast="char *")long s);
    
    @JniClass(flags={ClassFlag.STRUCT})
    static public class foo {
        
        public static final native int foo_sizeof ();
        public static final int SIZEOF = foo_sizeof();

        public int a;
        
        @JniField(cast="size_t")
        public long b;
        
        public byte c[] = new byte[20];

        @JniField(accessor="c[5]")
        public byte c5;
        
        @JniField(cast="void *")
        public long prev;
    }    
    
    public static final native void memmove (
            @JniArg(cast="void *") long dest, 
            @JniArg(cast="const void *", flags={NO_OUT, CRITICAL}) foo src, 
            @JniArg(cast="size_t") long size);
    
    public static final native void memmove (
            @JniArg(cast="void *", flags={NO_IN, CRITICAL}) foo dest, 
            @JniArg(cast="const void *") long src, 
            @JniArg(cast="size_t") long size);

    public static final native void print_foo(@JniArg(cast="struct foo *")long ptr);
    public static final native long foowork (@JniArg(cast="struct foo **") long[] foos, int count);

}
