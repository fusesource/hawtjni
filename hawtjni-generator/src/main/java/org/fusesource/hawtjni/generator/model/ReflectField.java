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

import static org.fusesource.hawtjni.generator.util.TextSupport.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;

import org.fusesource.hawtjni.runtime.FieldFlag;
import org.fusesource.hawtjni.runtime.JniField;
import org.fusesource.hawtjni.runtime.Pointer;
import org.fusesource.hawtjni.runtime.T32;

/**
 * 
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class ReflectField implements JNIField {
    
    private ReflectClass parent;
    private Field field;
    private ReflectType type;
    private JniField annotation;
    private HashSet<FieldFlag> flags;
    private boolean allowConversion;

    public ReflectField(ReflectClass parent, Field field) {
        this.parent = parent;
        this.field = field;
        lazyLoad();
    }

    public int hashCode() {
        return field.hashCode();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof ReflectField))
            return false;
        return ((ReflectField) obj).field.equals(field);
    }
    
    public String toString() {
        return field.toString();
    }

    ///////////////////////////////////////////////////////////////////
    // JNIField interface methods
    ///////////////////////////////////////////////////////////////////

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
        return type.asType32(allowConversion);
    }

    public JNIType getType64() {
        return type.asType64(allowConversion);
    }

    public String getAccessor() {
        return annotation == null ? "" : annotation.accessor();
    }

    public String getCast() {
        String rc = annotation == null ? "" : annotation.cast().trim();
        return cast(rc);
    }

    public boolean isPointer() {
        if( annotation == null ) {
            return false;
        }
        return annotation.pointer() == Pointer.DETERMINE_FROM_CAST ? getCast().endsWith("*)") : annotation.pointer()==Pointer.TRUE;
    }

    public String getConditional() {
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

    public boolean getFlag(FieldFlag flag) {
        return flags.contains(flag);
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
        this.type = new ReflectType(field.getType());
        this.annotation = this.field.getAnnotation(JniField.class);
        this.flags = new HashSet<FieldFlag>();
        if( this.annotation!=null ) {
            this.flags.addAll(Arrays.asList(this.annotation.flags()));
        }
        
        allowConversion = this.field.getAnnotation(T32.class)!=null;
    }

}
