/*******************************************************************************
 * Copyright (C) 2009-2011 FuseSource Corp.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.fusesource.hawtjni.runtime;

/**
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class PointerMath {

    private static final boolean bits32 = Library.getBitModel() == 32;

    final public static long add(long ptr, long n) {
        if(bits32) {
            return (int)(ptr + n);
        } else {
            return ptr + n;
        }
    }

}
