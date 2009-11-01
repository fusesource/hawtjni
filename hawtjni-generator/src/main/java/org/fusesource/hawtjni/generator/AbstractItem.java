/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.fusesource.hawtjni.generator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public abstract class AbstractItem implements JNIItem {

    HashMap<String, Object> params;

    static String[] split(String str, String separator) {
        return JNIGenerator.split(str, separator);
    }

    public abstract String flatten();
    public abstract void parse(String str);
    public abstract void setMetaData(String value);
    public abstract String getMetaData();

    void checkParams() {
        if (params != null)
            return;
        parse(getMetaData());
    }

    public boolean getFlag(String flag) {
        String[] flags = getFlags();
        for (int i = 0; i < flags.length; i++) {
            if (flags[i].equals(flag))
                return true;
        }
        return false;
    }


    public Object getParam(String key) {
        checkParams();
        Object value = params.get(key);
        return value == null ? "" : value;
    }
    public void setParam(String key, Object value) {
        checkParams();
        params.put(key, value);
        setMetaData(flatten());
    }

    public void setFlags(String[] flags) {
        setParam("flags", flags);
    }
    public String[] getFlags() {
        Object flags = getParam("flags");
        if (flags == null)
            return new String[0];
        if (flags instanceof String[])
            return (String[]) flags;
        String[] result = split((String) flags, " ");
        setParam("flags", result);
        return result;
    }

    public void setFlag(String flag, boolean value) {
        HashSet<String> set = new HashSet<String>(Arrays.asList(getFlags()));
        if (value) {
            set.add(flag);
        } else {
            set.remove(flag);
        }
        setFlags(set.toArray(new String[set.size()]));
    }

    public boolean getGenerate() {
        return !getFlag(FLAG_NO_GEN);
    }
    public void setGenerate(boolean value) {
        setFlag(FLAG_NO_GEN, !value);
    }

}
