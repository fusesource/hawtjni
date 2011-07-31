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
package org.fusesource.hawtjni.generator.model;

import java.util.List;

import org.fusesource.hawtjni.runtime.ClassFlag;

/**
 * 
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public interface JNIClass {

    public boolean getFlag(ClassFlag flag);

    public String getName();
    public String getSimpleName();
    public String getNativeName();

    public JNIClass getSuperclass();
    public List<JNIField> getDeclaredFields();
    public List<JNIMethod> getDeclaredMethods();
    public List<JNIMethod> getNativeMethods();
    

    public boolean getGenerate();
    public String getConditional();
}
