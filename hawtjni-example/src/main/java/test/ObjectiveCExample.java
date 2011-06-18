/*******************************************************************************
 * Copyright (C) 2009-2011 FuseSource Corp.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package test;

import org.fusesource.hawtjni.runtime.JniArg;
import org.fusesource.hawtjni.runtime.JniClass;
import org.fusesource.hawtjni.runtime.JniMethod;
import org.fusesource.hawtjni.runtime.Library;

import static org.fusesource.hawtjni.runtime.ArgFlag.*;
import static org.fusesource.hawtjni.runtime.MethodFlag.*;

/**
 * 
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
@JniClass(conditional="defined(__APPLE__)")
public class ObjectiveCExample {
    
    private static final Library LIBRARY = new Library("hawtjni-example", Example.class);    
    static {
        LIBRARY.load();
    }
    
    public static final void main(String args[]) {
        
        // Memory pool...
        long NSAutoreleasePool = objc_getClass("NSAutoreleasePool");        
        long pool = $($(NSAutoreleasePool, alloc), init);

        // Allocate and use a simple Objective C object
        long NSString = objc_getClass("NSString");
        long id = $(NSString, stringWithUTF8String, "Hello");        
        long value = $(id, length);        
        System.out.println("The length was: "+value);
        
        // Release the pool to release the allocations..
        $(pool, release);
    }
    
    

    public static final long stringWithUTF8String = sel_registerName("stringWithUTF8String:");
    public static final long release = sel_registerName("release");
    public static final long alloc = sel_registerName("alloc");    
    public static final long init = sel_registerName("init");    
    public static final long length = sel_registerName("length");    
    
    @JniMethod(cast="SEL", flags={POINTER_RETURN})
    public static final native long sel_registerName(String selectorName);
    
    @JniMethod(cast="id", flags={POINTER_RETURN})
    public static final native long objc_getClass(String className);
    
    @JniMethod(cast="id", flags={POINTER_RETURN}, accessor="objc_msgSend")
    public static final native long $(
            @JniArg(cast="id", flags={POINTER_ARG})long id, 
            @JniArg(cast="SEL", flags={POINTER_ARG})long sel
            );
    
    @JniMethod(cast="id", flags={POINTER_RETURN}, accessor="objc_msgSend")
    public static final native long $(
            @JniArg(cast="id", flags={POINTER_ARG})long id, 
            @JniArg(cast="SEL", flags={POINTER_ARG})long sel,
            String arg0);

}
