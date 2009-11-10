/*******************************************************************************
 * Copyright (c) 2009 Progress Software, Inc.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.fusesource.hawtjni.runtime;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * 
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
@Target({FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface JniField {

    String cast() default "";
    String accessor() default "";
    String exclude() default "";
    FieldFlag[] flags() default {};
    Pointer pointer() default Pointer.DETERMINE_FROM_CAST;
    
}
