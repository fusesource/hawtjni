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
import org.fusesource.hawtjni.generator.model.JNIType;
import org.fusesource.hawtjni.generator.model.ReflectClass;

/**
 * 
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class ConstantsGenerator extends JNIGenerator {

    public void generate(JNIClass clazz) {
        List<JNIField> fields = clazz.getDeclaredFields();
        generate(fields);
    }

    public void generate(List<JNIField> fields) {
        sortFields(fields);
        outputln("int main() {");
        for (JNIField field : fields) {
            if ((field.getModifiers() & Modifier.FINAL) == 0)
                continue;
            generate(field);
        }
        outputln("}");
    }

    public void generate(JNIField field) {
        JNIType type = field.getType();
        output("\tprintf(\"public static final ");
        output(field.getType().getTypeSignature3(false));
        output(" ");
        output(field.getName());
        output(" = ");
        if (type.isType("java.lang.String") || type.isType("[B"))
            output("\"%s\"");
        else
            output("0x%x");
        output(";\\n\", ");
        output(field.getName());
        outputln(");");
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java ConstantsGenerator <className1> <className2>");
            return;
        }
        try {
            ConstantsGenerator gen = new ConstantsGenerator();
            for (String clazzName : args) {
                Class<?> clazz = Class.forName(clazzName);
                gen.generate(new ReflectClass(clazz));
            }
        } catch (Exception e) {
            System.out.println("Problem");
            e.printStackTrace(System.out);
        }
    }

}
