/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.fusesource.hawtjni.generator.model;

import java.lang.reflect.Field;

import org.fusesource.hawtjni.runtime.Jni;
import org.fusesource.hawtjni.runtime.JniVT;

public class ReflectField extends ReflectItem implements JNIField {
    
    ReflectClass parent;
    Field field;
    ReflectType type32;
    ReflectType type64;

    public ReflectField(ReflectClass parent, Field field) {
        this.parent = parent;
        this.field = field;
        Class<?> clazz = field.getType();
        
        setJNI(field.getAnnotation(Jni.class));
        setJniVT(field.getAnnotation(JniVT.class));
        
        ReflectType type = new ReflectType(clazz);
        type32 = type.asType32(isVariableType());
        type64 = type.asType64(isVariableType());
    }

    public int hashCode() {
        return field.hashCode();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof ReflectField))
            return false;
        return ((ReflectField) obj).field.equals(field);
    }

    public JNIClass getDeclaringClass() {
        return parent;
    }

    public int getModifiers() {
        return field.getModifiers();
    }

    public String getName() {
        return field.getName();
    }

    public JNIType getType() {
        return type32;
    }

    public JNIType getType64() {
        return type64;
    }

    public String getAccessor() {
        return (String) getParam("accessor");
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

    public String getExclude() {
        return (String) getParam("exclude");
    }

    public void setAccessor(String str) {
        setParam("accessor", str);
    }

    public void setCast(String str) {
        setParam("cast", str);
    }

    public void setExclude(String str) {
        setParam("exclude", str);
    }

    public String toString() {
        return field.toString();
    }

}
