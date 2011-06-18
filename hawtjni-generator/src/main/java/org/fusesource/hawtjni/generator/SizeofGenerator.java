/*******************************************************************************
 * Copyright (C) 2009-2011 FuseSource Corp.
 * Copyright (c) 2004 IBM Corporation and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.fusesource.hawtjni.generator;

import java.lang.reflect.Modifier;
import java.util.List;

import org.fusesource.hawtjni.generator.model.JNIClass;
import org.fusesource.hawtjni.generator.model.JNIField;
import org.fusesource.hawtjni.generator.model.ReflectClass;

/**
 * 
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class SizeofGenerator extends JNIGenerator {

    public void generate(JNIClass clazz) {
        String className = clazz.getSimpleName();
        output("\tprintf(\"");
        output(className);
        output("=%d\\n\", sizeof(");
        output(className);
        outputln("));");
    }

    public void generate() {
        outputln("int main() {");
        super.generate();
        outputln("}");
    }

    public void generate(List<JNIField> fields) {
        sortFields(fields);
        for (JNIField field : fields) {
            if ((field.getModifiers() & Modifier.FINAL) == 0)
                continue;
            generate(field);
        }
    }

    public void generate(JNIField field) {
        output("\tprintf(\"");
        output(field.getName());
        output("=%d\\n\", sizeof(");
        output(field.getName());
        outputln("));");
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java SizeofGenerator <className1> <className2>");
            return;
        }
        try {
            SizeofGenerator gen = new SizeofGenerator();
            for (int i = 0; i < args.length; i++) {
                String clazzName = args[i];
                Class<?> clazz = Class.forName(clazzName);
                gen.generate(new ReflectClass(clazz));
            }
        } catch (Exception e) {
            System.out.println("Problem");
            e.printStackTrace(System.out);
        }
    }

}
