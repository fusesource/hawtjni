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

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;

import org.fusesource.hawtjni.runtime.ArgFlag;
import org.fusesource.hawtjni.runtime.JniArg;
import org.fusesource.hawtjni.runtime.T32;

import static org.fusesource.hawtjni.generator.util.TextSupport.*;
import static org.fusesource.hawtjni.runtime.ArgFlag.*;

/**
 * 
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class ReflectParameter implements JNIParameter {
    
    private ReflectMethod method;
    private ReflectType type;
    private int parameter;
    
    private JniArg annotation;
    private boolean allowConversion;
    private HashSet<ArgFlag> flags;

    public ReflectParameter(ReflectMethod method, int parameter, Annotation[] annotations) {
        this.method = method;
        this.parameter = parameter;
        this.type = new ReflectType(method.getWrapedMethod().getParameterTypes()[parameter]);
        this.flags = new HashSet<ArgFlag>();
        if( annotations!=null ) {
            for (Annotation annotation : annotations) {
                if( annotation instanceof JniArg ) {
                    this.annotation = (JniArg) annotation;
                    this.flags.addAll(Arrays.asList(this.annotation.flags()));
                } else if( annotation instanceof T32 ) {
                    this.allowConversion = true;
                }
            }
        }
    }

    public String getCast() {
        String rc = annotation == null ? "" : annotation.cast();
        return cast(rc);
    }

    public boolean isPointer() {
        if( annotation == null ) {
            return false;
        }
        return getFlag(POINTER_ARG) || ( type.getWrappedClass() == Long.TYPE && getCast().endsWith("*)") );
    }

    public JNIMethod getMethod() {
        return method;
    }

    public boolean getFlag(ArgFlag flag) {
        return flags.contains(flag);
    }

    public JNIType getType32() {
        return type.asType32(allowConversion);
    }

    public JNIType getType64() {
        return type.asType64(allowConversion);
    }

    public JNIClass getTypeClass() {
        ReflectType type = (ReflectType) getType32();
        return new ReflectClass(type.getWrappedClass());
    }

    public int getParameter() {
        return parameter;
    }

}
