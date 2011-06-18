/*******************************************************************************
 * Copyright (C) 2009-2011 FuseSource Corp.
 * Copyright (c) 2004, 2007 IBM Corporation and others.
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
import org.fusesource.hawtjni.generator.model.JNIMethod;

/**
 * 
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class StatsGenerator extends JNIGenerator {

    boolean header;

    public StatsGenerator(boolean header) {
        this.header = header;
    }

    public void generateCopyright() {
        outputln(fixDelimiter(getCopyright()));
    }

    public void generateIncludes() {
        if (!header) {
            outputln("#include \"hawtjni.h\"");
            outputln("#include \""+getOutputName()+"_stats.h\"");
            outputln();
        }
    }

    public void generate(JNIClass clazz) {
        if (header) {
            generateHeaderFile(clazz);
        } else {
            generateSourceFile(clazz);
        }
    }

    void generateHeaderFile(JNIClass clazz) {
        generateNATIVEMacros(clazz);
        List<JNIMethod> methods = clazz.getDeclaredMethods();
        sortMethods(methods);
        generateFunctionEnum(methods);
    }

    void generateNATIVEMacros(JNIClass clazz) {
        String className = clazz.getSimpleName();
        outputln("#ifdef NATIVE_STATS");
        output("extern int ");
        output(className);
        outputln("_nativeFunctionCount;");
        output("extern int ");
        output(className);
        outputln("_nativeFunctionCallCount[];");
        output("extern char* ");
        output(className);
        outputln("_nativeFunctionNames[];");
        output("#define ");
        output(className);
        output("_NATIVE_ENTER(env, that, func) ");
        output(className);
        outputln("_nativeFunctionCallCount[func]++;");
        output("#define ");
        output(className);
        outputln("_NATIVE_EXIT(env, that, func) ");
        outputln("#else");
        output("#ifndef ");
        output(className);
        outputln("_NATIVE_ENTER");
        output("#define ");
        output(className);
        outputln("_NATIVE_ENTER(env, that, func) ");
        outputln("#endif");
        output("#ifndef ");
        output(className);
        outputln("_NATIVE_EXIT");
        output("#define ");
        output(className);
        outputln("_NATIVE_EXIT(env, that, func) ");
        outputln("#endif");
        outputln("#endif");
        outputln();
    }

    void generateSourceFile(JNIClass clazz) {
        outputln("#ifdef NATIVE_STATS");
        outputln();
        List<JNIMethod> methods = clazz.getDeclaredMethods();
        int methodCount = 0;
        for (JNIMethod method : methods) {
            if ((method.getModifiers() & Modifier.NATIVE) == 0)
                continue;
            methodCount++;
        }
        String className = clazz.getSimpleName();
        output("int ");
        output(className);
        output("_nativeFunctionCount = ");
        output(String.valueOf(methodCount));
        outputln(";");
        output("int ");
        output(className);
        output("_nativeFunctionCallCount[");
        output(String.valueOf(methodCount));
        outputln("];");
        output("char * ");
        output(className);
        outputln("_nativeFunctionNames[] = {");
        sortMethods(methods);
        for (JNIMethod method : methods) {
            if ((method.getModifiers() & Modifier.NATIVE) == 0)
                continue;
            String function = getFunctionName(method), function64 = getFunctionName(method, method.getParameterTypes64());
            if (!function.equals(function64)) {
                output("#ifndef ");
                output(JNI64);
                outputln();
            }
            output("\t\"");
            output(function);
            outputln("\",");
            if (!function.equals(function64)) {
                outputln("#else");
                output("\t\"");
                output(function64);
                outputln("\",");
                outputln("#endif");
            }
            if (progress != null)
                progress.step();
        }
        outputln("};");
        outputln();
        generateStatsNatives(className);
        outputln();
        outputln("#endif");
    }

    void generateStatsNatives(String className) {
        outputln("#define STATS_NATIVE(func) Java_org_fusesource_hawtjni_runtime_NativeStats_##func");
        outputln();

        output("JNIEXPORT jint JNICALL STATS_NATIVE(");
        output(toC(className + "_GetFunctionCount"));
        outputln(")");
        outputln("\t(JNIEnv *env, jclass that)");
        outputln("{");
        output("\treturn ");
        output(className);
        outputln("_nativeFunctionCount;");
        outputln("}");
        outputln();

        output("JNIEXPORT jstring JNICALL STATS_NATIVE(");
        output(toC(className + "_GetFunctionName"));
        outputln(")");
        outputln("\t(JNIEnv *env, jclass that, jint index)");
        outputln("{");
        output("\treturn ");
        if (isCPP) {
            output("env->NewStringUTF(");
        } else {
            output("(*env)->NewStringUTF(env, ");
        }
        output(className);
        outputln("_nativeFunctionNames[index]);");
        outputln("}");
        outputln();

        output("JNIEXPORT jint JNICALL STATS_NATIVE(");
        output(toC(className + "_GetFunctionCallCount"));
        outputln(")");
        outputln("\t(JNIEnv *env, jclass that, jint index)");
        outputln("{");
        output("\treturn ");
        output(className);
        outputln("_nativeFunctionCallCount[index];");
        outputln("}");
    }

    void generateFunctionEnum(List<JNIMethod> methods) {
        if (methods.isEmpty())
            return;
        outputln("typedef enum {");
        for (JNIMethod method : methods) {
            if ((method.getModifiers() & Modifier.NATIVE) == 0)
                continue;
            String function = getFunctionName(method), function64 = getFunctionName(method, method.getParameterTypes64());
            if (!function.equals(function64)) {
                output("#ifndef ");
                output(JNI64);
                outputln();
            }
            output("\t");
            output(method.getDeclaringClass().getSimpleName()+"_"+function);
            outputln("_FUNC,");
            if (!function.equals(function64)) {
                outputln("#else");
                output("\t");
                output(method.getDeclaringClass().getSimpleName()+"_"+function64);
                outputln("_FUNC,");
                outputln("#endif");
            }
            if (progress != null)
                progress.step();
        }
        JNIClass clazz = methods.get(0).getDeclaringClass();
        output("} ");
        output(clazz.getSimpleName());
        outputln("_FUNCS;");
    }

}
