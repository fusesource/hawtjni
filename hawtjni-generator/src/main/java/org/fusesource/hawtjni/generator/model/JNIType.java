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

/**
 * 
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public interface JNIType {

    public boolean isPrimitive();
    public boolean isArray();
    public JNIType getComponentType();
    public boolean isType(String type);
    public String getName();
    public String getSimpleName();
    public String getNativeName();
    public String getTypeSignature(boolean define);
    public String getTypeSignature1(boolean define);
    public String getTypeSignature2(boolean define);
    public String getTypeSignature3(boolean define);
    public String getTypeSignature4(boolean define, boolean struct);

}
