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

import org.fusesource.hawtjni.runtime.ArgFlag;
import org.fusesource.hawtjni.runtime.MethodFlag;

/**
 * 
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public interface JNIMethod {

    public boolean getFlag(MethodFlag flag);

    public String getName();

    public int getModifiers();

    public boolean isNativeUnique();

    public JNIType getReturnType32();

    public JNIType getReturnType64();

    public List<JNIParameter> getParameters();

    public List<JNIType> getParameterTypes();

    public List<JNIType> getParameterTypes64();

    public JNIClass getDeclaringClass();

    public String getAccessor();

    public String getConditional();

    public String getCopy();

    public String[] getCallbackTypes();
    public ArgFlag[][] getCallbackFlags();

    public String getCast();
    
    public boolean isPointer();
}
