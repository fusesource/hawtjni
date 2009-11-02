/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.fusesource.hawtjni.generator.model;

import java.io.File;
import java.lang.annotation.Annotation;

import org.fusesource.hawtjni.runtime.Jni;
import org.fusesource.hawtjni.runtime.JniVT;

public class ReflectParameter extends ReflectItem implements JNIParameter {
    ReflectMethod method;

    int parameter;

    public ReflectParameter(ReflectMethod method, int parameter, Annotation[] annotations) {
        this.method = method;
        this.parameter = parameter;
        if( annotations!=null ) {
            for (Annotation annotation : annotations) {
                if( annotation instanceof Jni ) {
                    setJNI((Jni)annotation);
                } else if( annotation instanceof JniVT ) {
                    setJniVT((JniVT) annotation);
                }
            }
        }
    }

    public String getCast() {
        String cast = ((String) getParam("cast")).trim();
        if (cast.length() > 0) {
            if (!cast.startsWith("("))
                cast = "(" + cast;
            if (!cast.endsWith(")"))
                cast = cast + ")";
        }
        return cast;
    }

    public JNIMethod getMethod() {
        return method;
    }

    public JNIClass getTypeClass() {
        ReflectType type = (ReflectType) getType();
        ReflectClass declaringClass = method.declaringClass;
        String sourcePath = declaringClass.sourcePath;
        sourcePath = new File(sourcePath).getParent() + "/" + type.getSimpleName() + ".java";
        return new ReflectClass(type.clazz);
    }

    public JNIType getType() {
        return method.getParameterTypes()[parameter];
    }

    public JNIType getType64() {
        return method.getParameterTypes64()[parameter];
    }

    public int getParameter() {
        return parameter;
    }

    public void setCast(String str) {
        setParam("cast", str);
    }

}
