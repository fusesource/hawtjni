/*******************************************************************************
 * Copyright (c) 2009 Progress Software, Inc.
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
public enum ClassFlag {
    /** 
     * Indicate that the item should not be generated. For example, 
     * custom natives are coded by hand. 
     */
    NO_GEN,
    
    /**
     * Indicate that the platform source is in C++
     */
    CPP,
    
    /**
     * Indicate that this class will define a structure
     */
    STRUCT,
    
    /**
     * Indicate that the struct should get zeroed out before
     * setting any of it's fields.  Comes in handy when 
     * you don't map all the struct fields to java fields but
     * still want the un mapped fields initialized. 
     */
    ZERO_OUT,
}