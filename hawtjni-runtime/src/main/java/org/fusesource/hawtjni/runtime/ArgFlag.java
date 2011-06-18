/*******************************************************************************
 * Copyright (C) 2009-2011 FuseSource Corp.
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.fusesource.hawtjni.runtime;

/**
 * 
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public enum ArgFlag {
    
    /**
     * Indicate that a native method parameter is an out only variable. 
     * This only makes sense if the parameter is a structure or an array 
     * of primitives. It is an optimization to avoid copying the java 
     * memory to C memory on the way in. 
     */
    NO_IN,
    
    /**
     * Indicate that a native method parameter is an in only variable. 
     * This only makes sense if the parameter is a structure or an array 
     * of primitives. It is an optimization to avoid copying the C memory 
     * from java memory on the way out.
     */
    NO_OUT,
    
    /**
     * Indicate that GetPrimitiveArrayCritical() should be used instead 
     * of Get<PrimitiveType>ArrayElements() when transferring array of 
     * primitives from/to C. This is an optimization to avoid copying 
     * memory and must be used carefully. It is ok to be used in
     * MoveMemory() and memmove() natives. 
     */
    CRITICAL,
    
    /**
     * Indicate that the associated C local variable for a native method 
     * parameter should be initialized with zeros. 
     */
    INIT,
    
    /**
     * Indicate that the parameter is a pointer.
     */
    POINTER_ARG,

    /**
     * Indicate that a structure parameter should be passed by value 
     * instead of by reference. This dereferences the parameter by 
     * prepending *. The parameter must not be NULL.
     */
    BY_VALUE,
    
    /**
     * Indicate that GetStringChars()should be used instead of 
     * GetStringUTFChars() to get the characters of a java.lang.String 
     * passed as a parameter to native methods.
     */
    UNICODE,
    
    /**
     * Indicate that the parameter of a native method is the sentinel 
     * (last parameter of a variable argument C function). The generated 
     * code is always the literal NULL. Some compilers expect the sentinel 
     * to be the literal NULL and output a warning if otherwise.
     */
    SENTINEL,
        
    /**
     * Indicate that the native parameter is a C# managed object.
     */
    CS_OBJECT,

}