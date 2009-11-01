/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.fusesource.hawtjni.generator;

import java.util.Calendar;

public class MetaData {
    
    public static final String END_YEAR_TAG = "%END_YEAR%";

    String copyright;

    public String getCopyright() {
        if (copyright == null)
            return "";
        
        int index = copyright.indexOf(END_YEAR_TAG);
        if (index != -1) {
            String temp = copyright.substring(0, index);
            temp += Calendar.getInstance().get(Calendar.YEAR);
            temp += copyright.substring(index + END_YEAR_TAG.length());
            copyright = temp;
        }
        
        return copyright;
    }

}
