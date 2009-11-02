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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.fusesource.hawtjni.runtime.Jni;
import org.fusesource.hawtjni.runtime.JniVT;

public class ReflectMethod extends ReflectItem implements JNIMethod {

    Method method;
    ReflectType returnType32, returnType64;
    ReflectType[] paramTypes32, paramTypes64;
    ReflectClass declaringClass;
    ReflectParameter[] parameters;
    Boolean unique;

    public ReflectMethod(ReflectClass declaringClass, Method method) {
        this.declaringClass = declaringClass;
        this.method = method;
        
        this.setJNI(method.getAnnotation(Jni.class));
        this.setJniVT(method.getAnnotation(JniVT.class));
        
        Class<?> returnType = method.getReturnType();
        Class<?>[] paramTypes = method.getParameterTypes();
        
        this.paramTypes32 = new ReflectType[paramTypes.length];
        this.paramTypes64 = new ReflectType[paramTypes.length];
        this.parameters = new ReflectParameter[paramTypes.length];

        ReflectType type = new ReflectType(returnType);
        this.returnType32 = type.asType32(isVariableType());
        this.returnType64 = type.asType32(isVariableType());
        
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < this.paramTypes32.length; i++) {
            this.parameters[i] = new ReflectParameter(this, i, parameterAnnotations[i]);
            type = new ReflectType(paramTypes[i]);
            this.paramTypes32[i] = type.asType32( this.parameters[i].isVariableType() );
            this.paramTypes64[i] = type.asType64( this.parameters[i].isVariableType() );
        }
        
    }

    public int hashCode() {
        return method.hashCode();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof ReflectMethod))
            return false;
        return ((ReflectMethod) obj).method.equals(method);
    }

    public JNIClass getDeclaringClass() {
        return declaringClass;
    }

    public int getModifiers() {
        return method.getModifiers();
    }

    public String getName() {
        return method.getName();
    }

    public boolean isNativeUnique() {
        if (unique != null)
            return unique.booleanValue();
        boolean result = true;
        String name = getName();
        JNIMethod[] methods = declaringClass.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            JNIMethod mth = methods[i];
            if ((mth.getModifiers() & Modifier.NATIVE) != 0 && this != mth && !this.equals(mth) && name.equals(mth.getName())) {
                result = false;
                break;
            }
        }
        unique = new Boolean(result);
        return result;
    }

    public JNIType[] getParameterTypes() {
        return paramTypes32;
    }

    public JNIType[] getParameterTypes64() {
        return paramTypes64;
    }

    public JNIParameter[] getParameters() {
        return parameters;
    }

    public JNIType getReturnType() {
        return returnType32;
    }

    public JNIType getReturnType64() {
        return returnType64;
    }

    public String getAccessor() {
        return (String) getParam("accessor");
    }

    public String getExclude() {
        return (String) getParam("exclude");
    }

    public void setAccessor(String str) {
        setParam("accessor", str);
    }

    public void setExclude(String str) {
        setParam("exclude", str);
    }

    public String toString() {
        return method.toString();
    }
}
