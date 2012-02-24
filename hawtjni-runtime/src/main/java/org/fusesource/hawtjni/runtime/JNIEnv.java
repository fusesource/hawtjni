/**
 * Copyright (C) 2010, FuseSource Corp.  All rights reserved.
 */
package org.fusesource.hawtjni.runtime;

/**
 * <p>
 *  This is a marker class. Methods that take this as an argument
 *  will receive that actual native 'JNIEnv *' value.  Since this
 *  class cannot be instantiated, Java callers must pass null
 *  for the value.
 * </p>
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class JNIEnv {
    private JNIEnv() {}
}
