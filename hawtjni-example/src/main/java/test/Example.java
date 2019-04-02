/*******************************************************************************
 * Copyright (C) 2009-2011 FuseSource Corp.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package test;

import java.util.Arrays;

import org.fusesource.hawtjni.runtime.*;

import static org.fusesource.hawtjni.runtime.ArgFlag.*;
import static org.fusesource.hawtjni.runtime.FieldFlag.*;
import static org.fusesource.hawtjni.runtime.MethodFlag.*;

/**
 * 
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
@JniClass
public class Example {
    
    private static final Library LIBRARY = new Library("hawtjni-example", Example.class);    
	static {
        LIBRARY.load();
        init();
	}

    public static final void main(String args[]) {

        System.out.println("Checking Operating System Constants:");
        System.out.println(" O_RDONLY: "+O_RDONLY);
        System.out.println(" O_WRONLY: "+O_WRONLY);
        System.out.println("   O_RDWR: "+O_RDWR);
        System.out.println("");

        System.out.println("Allocating c structures on the heap...");
        int COUNT = 10;
        // We track memory pointers with longs.
        long []ptrArray = new long[COUNT];        
        long last=0;
        for( int i=0; i < COUNT; i++ ) {
            // Allocate heap space of the structure..
            ptrArray[i] = malloc(bar.SIZEOF);

            // Configure some data for a structure...
            bar f = new bar();
            f.a = i;
            f.b = 1;
            
            byte[] src = "hello world".getBytes();
            System.arraycopy(src, 0, f.c, 0, src.length);
            
            f.c5 = 0;
            f.prev = last;
            
            // Copy the data values into the allocated space.
            memmove(ptrArray[i], f, bar.SIZEOF);
            
            last = ptrArray[i];
        }
        
        // Display a couple of structures...
        System.out.println("Dump of the first 2 structures:");
        print_foo(ptrArray[0]);
        print_foo(ptrArray[1]);
        
        System.out.println("Passing a pointer array to a c function...");
        long rc = foowork(ptrArray, COUNT);
        System.out.println("Function result (expecting 55): "+rc);
        
        System.out.println("freein up allocated memory.");
        for( int i=0; i < COUNT; i++ ) {
            free(ptrArray[i]);
        }
    }

    // Example of how to load constants.
    @JniMethod(flags={CONSTANT_INITIALIZER})
    private static final native void init();

    @JniField(flags={CONSTANT})
    public static int O_RDONLY;
    @JniField(flags={CONSTANT})
    public static int O_WRONLY;
    @JniField(flags={CONSTANT})
    public static int O_RDWR;
    
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
            @JniArg(cast="const void *", flags={NO_OUT, CRITICAL}) long[] src, 
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
            @JniArg(cast="void *", flags={NO_IN, CRITICAL}) long[] dest, 
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
    
    @JniClass(name="foo", flags={ClassFlag.STRUCT})
    static public class bar {
        
        static {
            LIBRARY.load();
            init();
        }
        
        @JniMethod(flags={CONSTANT_INITIALIZER})
        private static final native void init();

//        public static final native int foo_sizeof ();
        
        @JniField(flags={CONSTANT}, accessor="sizeof(struct foo)")
        public static int SIZEOF;

        public int a;
        
        @JniField(cast="size_t")
        public long b;
        
        public byte c[] = new byte[20];

        @JniField(accessor="c[5]")
        public byte c5;
        
        @JniField(cast="struct foo *")
        public long prev;

        @JniField(getter = "get_d()", setter = "set_d()", flags = { GETTER_NONMEMBER, SETTER_NONMEMBER })
        private float d;

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + a;
            result = prime * result + (int) (b ^ (b >>> 32));
            result = prime * result + Arrays.hashCode(c);
            result = prime * result + c5;
            result = prime * result + (int) (prev ^ (prev >>> 32));
            result = prime * result + Float.valueOf(d).hashCode();
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            bar other = (bar) obj;
            if (a != other.a)
                return false;
            if (b != other.b)
                return false;
            if (!Arrays.equals(c, other.c))
                return false;
            if (c5 != other.c5)
                return false;
            if (prev != other.prev)
                return false;
            if (d != other.d) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "foo [a=" + a + ", b=" + b + ", c=" + Arrays.toString(c) + ", c5=" + c5 + ", prev=" + prev + ", d=" + d + "]";
        }
        
    }    
    
    public static final native void memmove (
            @JniArg(cast="void *") long dest, 
            @JniArg(cast="const void *", flags={NO_OUT, CRITICAL}) bar src,
            @JniArg(cast="size_t") long size);
    
    public static final native void memmove (
            @JniArg(cast="void *", flags={NO_IN, CRITICAL}) bar dest,
            @JniArg(cast="const void *") long src, 
            @JniArg(cast="size_t") long size);

    public static final native void print_foo(@JniArg(cast="struct foo *")long ptr);
    public static final native long foowork (@JniArg(cast="struct foo **") long[] foos, int count);

    @JniMethod(cast = "struct foo *")
    public static final native long foo_add(@JniArg(cast="struct foo *")long ptr, int count);

    @JniMethod(cast = "char *")
    public static final native long char_add(@JniArg(cast="char *")long ptr, int count);

    @JniClass(flags={ClassFlag.STRUCT, ClassFlag.TYPEDEF})
    static public class point {
        static {
            LIBRARY.load();
            init();
        }
        
        @JniMethod(flags={CONSTANT_INITIALIZER})
        private static final native void init();

        @JniField(flags={CONSTANT}, accessor="sizeof(point)")
        public static int SIZEOF;

        public int x;
        public int y;
    }
    
    public static final native void callmeback(
            @JniArg(cast="void (*)(int)", flags = ArgFlag.POINTER_ARG)
            long ptr);

    @JniClass(flags={ClassFlag.STRUCT, ClassFlag.CPP})
    static class Range {
        static {
            LIBRARY.load();
        }

        @JniMethod(flags={MethodFlag.CPP_NEW})
        public static final native long Range();

        @JniMethod(flags={MethodFlag.CPP_NEW})
        public static final native long Range(int start, int end);

        @JniMethod(flags={MethodFlag.CPP_DELETE})
        public static final native void delete(long ptr);

        @JniMethod(flags={MethodFlag.CPP_METHOD})
        public static final native void dump(long ptr);


    }

    public static final native void passingtheenv (String msg, JNIEnv env);

    @JniClass(flags={ClassFlag.STRUCT})
    static class ClassWithAccessors {
        static {
            LIBRARY.load();
        }

        @JniField(getter = "get_e()", setter = "set_e()")
        private float e;


    }
}
