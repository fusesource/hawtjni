/*******************************************************************************
 * Copyright (c) 2009 Progress Software, Inc.
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.fusesource.hawtjni.generator.model;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.fusesource.hawtjni.runtime.ClassFlag;
import org.fusesource.hawtjni.runtime.JniClass;

/**
 * 
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class ReflectClass implements JNIClass {
    
    private Class<?> clazz;
    private ArrayList<ReflectField> fields;
    private ArrayList<ReflectMethod> methods;
    private JniClass annotation;
    private HashSet<ClassFlag> flags;

    public ReflectClass(Class<?> clazz) {
        this.clazz = clazz;
    }

    public String toString() {
        return clazz.toString();
    }
    public int hashCode() {
        return clazz.hashCode();
    }
    public boolean equals(Object obj) {
        if (!(obj instanceof ReflectClass))
            return false;
        return ((ReflectClass) obj).clazz.equals(clazz);
    }
    
    public Class<?> getWrapedClass() {
        return clazz;
    }

    ///////////////////////////////////////////////////////////////////
    // JNIClass interface methods
    ///////////////////////////////////////////////////////////////////
    
    public String getName() {
        return clazz.getName();
    }

    public JNIClass getSuperclass() {
        return new ReflectClass(clazz.getSuperclass());
    }
    
    public String getSimpleName() {
        return clazz.getSimpleName();
    }
    
    public List<JNIField> getDeclaredFields() {
        lazyLoad();
        return new ArrayList<JNIField>(fields);
    }

    public List<JNIMethod> getDeclaredMethods() {
        lazyLoad();
        return new ArrayList<JNIMethod>(methods);
    }

    public List<JNIMethod> getNativeMethods() {
        ArrayList<JNIMethod> rc = new ArrayList<JNIMethod>();
        for (JNIMethod method : getDeclaredMethods()) {
            if ((method.getModifiers() & Modifier.NATIVE) == 0)
                continue;
            rc.add(method);
        }
        return rc;
    }

    public String getConditional() {
        lazyLoad();
        return annotation == null ? "" : annotation.conditional();
    }

    public boolean getGenerate() {
        return !getFlag(ClassFlag.CLASS_SKIP);
    }
    
    public boolean getFlag(ClassFlag flag) {
        lazyLoad();
        return flags.contains(flag);
    }

    ///////////////////////////////////////////////////////////////////
    // Helper methods
    ///////////////////////////////////////////////////////////////////

    private void lazyLoad() {
        if (fields != null)
            return;
        
        this.annotation = this.clazz.getAnnotation(JniClass.class);
        this.flags = new HashSet<ClassFlag>();
        if( this.annotation!=null ) {
            this.flags.addAll(Arrays.asList(this.annotation.flags()));
        }
        
        Field[] fields = clazz.getDeclaredFields();
        this.fields = new ArrayList<ReflectField>(fields.length);
        for (Field field : fields) {
            this.fields.add(new ReflectField(this, field));
        }
        Method[] methods = clazz.getDeclaredMethods();
        this.methods = new ArrayList<ReflectMethod>(methods.length);
        for (int i = 0; i < methods.length; i++) {
            this.methods.add(new ReflectMethod(this, methods[i]));
        }
    }

}
