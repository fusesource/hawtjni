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

import org.fusesource.hawtjni.runtime.ArgFlag;

/**
 * 
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public interface JNIParameter {

    public boolean getFlag(ArgFlag flag);
    public String getCast();
    public boolean isPointer();

    public JNIMethod getMethod();
    public int getParameter();
    public JNIClass getTypeClass();
    public JNIType getType32();
    public JNIType getType64();

}
