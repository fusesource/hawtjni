/*******************************************************************************
 * Copyright (C) 2009-2011 FuseSource Corp.
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
public enum MethodFlag {
    /** 
     * Indicate that the item should not be generated. For example, 
     * custom natives are coded by hand. 
     */
    METHOD_SKIP,
    
    /**
     * Indicate that a native method should be looked up dynamically. It 
     * is useful when having a dependence on a given library is not 
     * desirable. The library name is specified in the *_custom.h file.
     */
    DYNAMIC,
    
    /**
     * Indicate that the native method represents a constant or global 
     * variable instead of a function. This omits () from the generated 
     * code.
     */
    CONSTANT_GETTER,
    
    /**
     * Indicate that the C function should be casted to a prototype 
     * generated from the parameters of the native method. Useful for 
     * variable argument C functions.
     */
    CAST,
    
    /**
     * Indicate that the native is part of the Java Native Interface. For 
     * example: NewGlobalRef(). 
     */
    JNI,
    
    /**
     * Indicate that the native method represents a structure global 
     * variable and the address of it should be returned to Java. This is 
     * done by prepending &.
     */
    ADDRESS,
    
    /**
     * Indicate that the native method is calling a C++ object's method.
     */
    CPP_METHOD,

    /**
     * Indicate that the native method is a C++ constructor that allocates 
     * an object on the heap.
     */
    CPP_NEW,
    
    /**
     * Indicate that the native method is a C++ destructor that 
     * deallocates an object from the heap.
     */
    CPP_DELETE,
    
    /**
     * Indicate that the native method is a C# constructor that allocates 
     * an object on the managed (i.e. garbage collected) heap.
     */
    CS_NEW,
    
    /**
     * Indicate that the native method's return value is a 
     * C# managed object.
     */
    CS_OBJECT,
    
    /**
     * Indicate that the native method represents a setter for a field in 
     * an object or structure
     */
    SETTER,
    
    /**
     * Indicate that the native method represents a getter for a field in 
     * an object or structure.
     */
    GETTER,
    
    /**
     * Indicate that the native method takes 2 arguments, a collection and 
     * an item, and the += operator is used to add the item to the 
     * collection.
     */
    ADDER,

    /**
     * Indicate that the return value is a pointer.
     */
    POINTER_RETURN,

    /**
     * Indicate that this method will be the constant initializer for
     * the class.  When called, it will set all the static constant fields
     * to the values defined in your platform.
     */
    CONSTANT_INITIALIZER,
}