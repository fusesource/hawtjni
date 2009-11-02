/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package test;

import org.fusesource.hawtjni.runtime.Jni;
import org.fusesource.hawtjni.runtime.JniClass;
import org.fusesource.hawtjni.runtime.JniVT;
import org.fusesource.hawtjni.runtime.Library;

@JniClass
public class Platform {
    
    public static final String PLATFORM = "osx";

    private static Library library = new Library("hawtjni-example", 1, 0, 0, PLATFORM);
	static {
	    library.load();
	}

    public static final void main(String args[]) {
        System.out.println("Allocating memory...");
        long ptr = malloc(1024*4);
        System.out.println(String.format("Allocated at: %x", ptr));
        free(ptr);
        System.out.println("Memory freed.");
    }

	
	@Jni("cast=(JAVA_PTR_CAST)")
    public static final native @JniVT long malloc (@JniVT long size);
    
    public static final native void free (@Jni("cast=(NATIVE_PTR_CAST)") @JniVT long ptr);
    
    public static final native  @JniVT long open (String file, int flags, int mode);
    
    
}
