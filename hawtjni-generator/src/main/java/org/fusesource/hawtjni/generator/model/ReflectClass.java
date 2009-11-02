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
import java.lang.reflect.Method;

import org.fusesource.hawtjni.runtime.JniClass;

public class ReflectClass extends ReflectItem implements JNIClass {
    
    Class<?> clazz;
    ReflectField[] fields;
    ReflectMethod[] methods;
    String sourcePath;

    public ReflectClass(Class<?> clazz) {
        this.clazz = clazz;
    }

    void checkMembers() {
        if (fields != null)
            return;
        Field[] fields = clazz.getDeclaredFields();
        this.fields = new ReflectField[fields.length];
        for (int i = 0; i < fields.length; i++) {
            this.fields[i] = new ReflectField(this, fields[i]);
        }
        Method[] methods = clazz.getDeclaredMethods();
        this.methods = new ReflectMethod[methods.length];
        for (int i = 0; i < methods.length; i++) {
            this.methods[i] = new ReflectMethod(this, methods[i]);
        }
    }

    public int hashCode() {
        return clazz.hashCode();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof ReflectClass))
            return false;
        return ((ReflectClass) obj).clazz.equals(clazz);
    }

    public JNIField[] getDeclaredFields() {
        checkMembers();
        JNIField[] result = new JNIField[fields.length];
        System.arraycopy(fields, 0, result, 0, result.length);
        return result;
    }

    public JNIMethod[] getDeclaredMethods() {
        checkMembers();
        JNIMethod[] result = new JNIMethod[methods.length];
        System.arraycopy(methods, 0, result, 0, result.length);
        return result;
    }

    public String getName() {
        return clazz.getName();
    }

    public JNIClass getSuperclass() {
        return new ReflectClass((Class<?>) clazz.getSuperclass());
    }

    String getSimpleName(Class<?> type) {
        String name = type.getName();
        int index = name.lastIndexOf('.') + 1;
        return name.substring(index, name.length());
    }

    public String getSimpleName() {
        return getSimpleName(clazz);
    }

    public String getExclude() {
        return (String) getParam("exclude");
    }

    public void setExclude(String str) {
        setParam("exclude", str);
    }


    public String toString() {
        return clazz.toString();
    }

    public String getInclude() {
        JniClass annotation = clazz.getAnnotation(JniClass.class);
        if( annotation==null ) {
            return null;
        }
        return annotation.include();
    }

}
