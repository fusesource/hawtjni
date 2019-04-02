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

import org.fusesource.hawtjni.runtime.FieldFlag;

/**
 * 
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public interface JNIField {

    public boolean getFlag(FieldFlag flag);

    public String getName();
    public int getModifiers();

    public JNIType getType();
    public JNIType getType64();

    public JNIClass getDeclaringClass();
    public JNIFieldAccessor getAccessor();
    public String getCast();
    public String getConditional();
    public boolean ignore();

    public boolean isSharedPointer();
    public boolean isPointer();
}
