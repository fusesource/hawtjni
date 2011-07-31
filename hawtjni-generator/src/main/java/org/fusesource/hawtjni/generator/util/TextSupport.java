/*******************************************************************************
 * Copyright (C) 2009-2011 FuseSource Corp.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.fusesource.hawtjni.generator.util;

/**
 * 
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class TextSupport {
    
    static public String cast(String cast) {
        cast = cast.trim();
        if (cast.length() > 0) {
            if (!cast.startsWith("(") || !cast.endsWith(")"))
                cast = "(" + cast + ")";
        }
        return cast;
    }
    
}
