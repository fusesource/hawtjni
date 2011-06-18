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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.fusesource.hawtjni.runtime.ArgFlag;
import org.fusesource.hawtjni.runtime.JniArg;
import org.fusesource.hawtjni.runtime.JniMethod;
import org.fusesource.hawtjni.runtime.MethodFlag;
import org.fusesource.hawtjni.runtime.T32;

import static org.fusesource.hawtjni.generator.util.TextSupport.*;
import static org.fusesource.hawtjni.runtime.MethodFlag.*;

/**
 * 
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class ReflectMethod implements JNIMethod {

    private ReflectClass declaringClass;
    private Method method;
    
    private List<JNIType> paramTypes32;
    private List<JNIType> paramTypes64;
    private List<JNIParameter> parameters;
    private boolean unique;
    private JniMethod annotation;

    private boolean allowConversion;
    private ReflectType returnType;
    
    private HashSet<MethodFlag> flags;

    public ReflectMethod(ReflectClass declaringClass, Method method) {
        this.declaringClass = declaringClass;
        this.method = method;
        lazyLoad();
    }

    public int hashCode() {
        return method.hashCode();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof ReflectMethod))
            return false;
        return ((ReflectMethod) obj).method.equals(method);
    }

    public String toString() {
        return method.toString();
    }
    
    public Method getWrapedMethod() {
        return method;
    }

    ///////////////////////////////////////////////////////////////////
    // JNIMethod interface methods
    ///////////////////////////////////////////////////////////////////

    public JNIClass getDeclaringClass() {
        return declaringClass;
    }

    public int getModifiers() {
        return method.getModifiers();
    }

    public String getName() {
        return method.getName();
    }
    
    public List<JNIParameter> getParameters() {
        lazyLoad();
        return parameters;
    }

    public List<JNIType> getParameterTypes() {
        lazyLoad();
        return paramTypes32;
    }

    public List<JNIType> getParameterTypes64() {
        lazyLoad();
        return paramTypes64;
    }
    
    public JNIType getReturnType32() {
        lazyLoad();
        return returnType.asType32(allowConversion);
    }

    public JNIType getReturnType64() {
        lazyLoad();
        return returnType.asType64(allowConversion);
    }
    
    public boolean getFlag(MethodFlag flag) {
        lazyLoad();
        return flags.contains(flag);
    }

    public String getCast() {
        lazyLoad();
        String rc = annotation == null ? "" : annotation.cast();
        return cast(rc);
    }

    public boolean isPointer() {
        lazyLoad();
        if( annotation == null ) {
            return false;
        }
        return getFlag(POINTER_RETURN) || ( returnType.getWrappedClass() == Long.TYPE && getCast().endsWith("*)") );
    }

    public String getCopy() {
        lazyLoad();
        return annotation == null ? "" : annotation.copy();
    }

    public String getAccessor() {
        lazyLoad();
        return annotation == null ? "" : annotation.accessor();
    }

    public String getConditional() {
        lazyLoad();
        
        String parentConditional = getDeclaringClass().getConditional();
        String myConditional = annotation == null ? null : emptyFilter(annotation.conditional());
        if( parentConditional!=null ) {
            if( myConditional!=null ) {
                return parentConditional+" && "+myConditional;
            } else {
                return parentConditional;
            }
        }
        return myConditional;
    }
    
    public boolean isNativeUnique() {
        lazyLoad();
        return unique;
    }

    public String[] getCallbackTypes() {
        lazyLoad();
        if( annotation==null ) {
            return new String[0];
        }

        JniArg[] callbackArgs = annotation.callbackArgs();
        String[] rc = new String[callbackArgs.length];
        for (int i = 0; i < rc.length; i++) {
            rc[i] = callbackArgs[i].cast();
        }
        
        return rc;
    }
    
    public ArgFlag[][] getCallbackFlags() {
        lazyLoad();
        if( annotation==null ) {
            return new ArgFlag[0][];
        }
        
        JniArg[] callbackArgs = annotation.callbackArgs();
        ArgFlag[][] rc = new ArgFlag[callbackArgs.length][];
        for (int i = 0; i < rc.length; i++) {
            rc[i] = callbackArgs[i].flags();
        }
        return rc;
    }


    ///////////////////////////////////////////////////////////////////
    // Helper methods
    ///////////////////////////////////////////////////////////////////
    static public String emptyFilter(String value) {
        if( value==null || value.length()==0 )
            return null;
        return value;
    }
    
    private void lazyLoad() {
        if( flags!=null ) {
            return;
        }
        
        this.annotation = this.method.getAnnotation(JniMethod.class);
        this.allowConversion = method.getAnnotation(T32.class)!=null;
        this.flags = new HashSet<MethodFlag>();
        if( this.annotation!=null ) {
            this.flags.addAll(Arrays.asList(this.annotation.flags()));
        }
        
        Class<?> returnType = method.getReturnType();
        Class<?>[] paramTypes = method.getParameterTypes();
        
        this.paramTypes32 = new ArrayList<JNIType>(paramTypes.length);
        this.paramTypes64 = new ArrayList<JNIType>(paramTypes.length);
        this.parameters = new ArrayList<JNIParameter>(paramTypes.length);
        this.returnType = new ReflectType(returnType);
        
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < paramTypes.length; i++) {
            ReflectParameter parameter = new ReflectParameter(this, i, parameterAnnotations[i]);
            this.parameters.add(parameter);
            this.paramTypes32.add( parameter.getType32() );
            this.paramTypes64.add( parameter.getType64() );
        }
        
        unique = true;
        Class<?> parent = ((ReflectClass)declaringClass).getWrapedClass();
        String name = method.getName();
        for (Method mth : parent.getDeclaredMethods() ) {
            if ( (mth.getModifiers()&Modifier.NATIVE) != 0 && method!=mth && !method.equals(mth) && name.equals(mth.getName())) {
                unique = false;
                break;
            }
        }

    }
}
