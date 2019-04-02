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
import java.util.ArrayList;
import java.util.List;

import org.fusesource.hawtjni.generator.model.JNIClass;
import org.fusesource.hawtjni.generator.model.JNIField;
import org.fusesource.hawtjni.generator.model.JNIFieldAccessor;
import org.fusesource.hawtjni.generator.model.JNIMethod;
import org.fusesource.hawtjni.generator.model.JNIParameter;
import org.fusesource.hawtjni.generator.model.JNIType;
import org.fusesource.hawtjni.runtime.ArgFlag;
import org.fusesource.hawtjni.runtime.ClassFlag;
import org.fusesource.hawtjni.runtime.FieldFlag;
import org.fusesource.hawtjni.runtime.MethodFlag;

import static org.fusesource.hawtjni.runtime.MethodFlag.*;

/**
 * 
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class NativesGenerator extends JNIGenerator {

    boolean enterExitMacro;

    public NativesGenerator() {
        enterExitMacro = true;
    }

    public void generateCopyright() {
        outputln(fixDelimiter(getCopyright()));
    }

    public void generateIncludes() {
        String outputName = getOutputName();
        outputln("#include \"" + outputName + ".h\"");
        outputln("#include \"hawtjni.h\"");
        outputln("#include \"" + outputName + "_structs.h\"");
        outputln("#include \"" + outputName + "_stats.h\"");
        outputln();
    }

    public void generate(JNIClass clazz) {
        List<JNIMethod> methods = clazz.getNativeMethods();
        if( methods.isEmpty() ) {
            return;
        }
        sortMethods(methods);
        generateNativeMacro(clazz);
        generate(methods);
    }

    public void generate(List<JNIMethod> methods) {
        sortMethods(methods);
        for (JNIMethod method : methods) {
            if ((method.getModifiers() & Modifier.NATIVE) == 0)
                continue;
            generate(method);
            if (progress != null)
                progress.step();
        }
    }

    boolean isStruct(ArgFlag flags[]) {
        for (ArgFlag flag : flags) {
            if (flag.equals(ArgFlag.BY_VALUE))
                return true;
        }
        return false;
    }

    void generateCallback(JNIMethod method, String function, List<JNIParameter> params, JNIType returnType) {
        output("static jintLong ");
        output(function);
        outputln(";");
        output("static ");
        String[] types = method.getCallbackTypes();
        ArgFlag[][] flags = method.getCallbackFlags();
        output(types[0]);
        output(" ");
        output("proc_");
        output(function);
        output("(");
        boolean first = true;
        for (int i = 1; i < types.length; i++) {
            if (!first)
                output(", ");
            output(types[i]);
            output(" ");
            output("arg");
            output(String.valueOf(i - 1));
            first = false;
        }
        outputln(") {");

        output("\t");
        if (isStruct(flags[0])) {
            output(types[0]);
            output("* lprc = ");
        } else if (!types[0].equals("void")) {
            output("return ");
        }
        output("((");
        output(types[0]);
        if (isStruct(flags[0]))
            output("*");
        output(" (*)(");
        first = true;
        for (int i = 1; i < types.length; i++) {
            if (!first)
                output(", ");
            first = false;
            output(types[i]);
            if (isStruct(flags[i]))
                output("*");
        }
        output("))");
        output(function);
        output(")(");
        first = true;
        for (int i = 1; i < types.length; i++) {
            if (!first)
                output(", ");
            first = false;
            if (isStruct(flags[i]))
                output("&");
            output("arg");
            output(String.valueOf(i - 1));
        }
        outputln(");");
        if (isStruct(flags[0])) {
            output("\t");
            output(types[0]);
            outputln(" rc;");
            outputln("\tif (lprc) {");
            outputln("\t\trc = *lprc;");
            outputln("\t\tfree(lprc);");
            outputln("\t} else {");
            output("\t\tmemset(&rc, 0, sizeof(");
            output(types[0]);
            outputln("));");
            outputln("\t}");
            outputln("\treturn rc;");
        }
        outputln("}");

        output("static jintLong ");
        output(method.getName());
        outputln("(jintLong func) {");
        output("\t");
        output(function);
        outputln(" = func;");
        output("\treturn (jintLong)proc_");
        output(function);
        outputln(";");
        outputln("}");
    }
    
    private void generateConstantsInitializer(JNIMethod method) {
        JNIClass clazz = method.getDeclaringClass();
        ArrayList<JNIField> constants = getConstantFields(clazz);
        if( constants.isEmpty() ) {
            return;
        }
        
        if (isCPP) {
            output("extern \"C\" ");
        }
        outputln("JNIEXPORT void JNICALL "+clazz.getSimpleName()+"_NATIVE("+toC(method.getName())+")(JNIEnv *env, jclass that)");
        outputln("{");
        for (JNIField field : constants) {

            String conditional = field.getConditional();
            if (conditional!=null) {
                outputln("#if "+conditional);
            }
            JNIType type = field.getType(), type64 = field.getType64();
            boolean allowConversion = !type.equals(type64);
            
            String simpleName = type.getSimpleName();
            JNIFieldAccessor accessor = field.getAccessor();

            String fieldId = "(*env)->GetStaticFieldID(env, that, \""+field.getName()+"\", \""+type.getTypeSignature(allowConversion)+"\")";
            if (isCPP) {
                fieldId = "env->GetStaticFieldID(that, \""+field.getName()+"\", \""+type.getTypeSignature(allowConversion)+"\")";
            }

            if (type.isPrimitive()) {
                if (isCPP) {
                    output("\tenv->SetStatic"+type.getTypeSignature1(allowConversion)+"Field(that, "+fieldId +", ");
                } else {
                    output("\t(*env)->SetStatic"+type.getTypeSignature1(allowConversion)+"Field(env, that, "+fieldId +", ");
                }
                output("("+type.getTypeSignature2(allowConversion)+")");
                if( field.isPointer() ) {
                    output("(intptr_t)");
                }
                output(accessor.getter());
                output(");");
                
            } else if (type.isArray()) {
                JNIType componentType = type.getComponentType(), componentType64 = type64.getComponentType();
                if (componentType.isPrimitive()) {
                    outputln("\t{");
                    output("\t");
                    output(type.getTypeSignature2(allowConversion));
                    output(" lpObject1 = (");
                    output(type.getTypeSignature2(allowConversion));
                    if (isCPP) {
                        output(")env->GetStaticObjectField(that, ");
                    } else {
                        output(")(*env)->GetStaticObjectField(env, that, ");
                    }
                    output(field.getDeclaringClass().getSimpleName());
                    output(fieldId);
                    outputln(");");
                    if (isCPP) {
                        output("\tenv->Set");
                    } else {
                        output("\t(*env)->Set");
                    }
                    output(componentType.getTypeSignature1(!componentType.equals(componentType64)));
                    if (isCPP) {
                        output("ArrayRegion(lpObject1, 0, sizeof(");
                    } else {
                        output("ArrayRegion(env, lpObject1, 0, sizeof(");
                    }
                    output(accessor.getter());
                    output(")");
                    if (!componentType.isType("byte")) {
                        output(" / sizeof(");
                        output(componentType.getTypeSignature2(!componentType.equals(componentType64)));
                        output(")");
                    }
                    output(", (");
                    output(type.getTypeSignature4(allowConversion, false));
                    output(")");
                    output(accessor.getter());
                    outputln(");");
                    output("\t}");
                } else {
                    throw new Error("not done");
                }
            } else {
                outputln("\t{");
                if (isCPP) {
                    output("\tjobject lpObject1 = env->GetStaticObjectField(that, ");
                } else {
                    output("\tjobject lpObject1 = (*env)->GetStaticObjectField(env, that, ");
                }
                output(field.getDeclaringClass().getSimpleName());
                output("Fc.");
                output(field.getName());
                outputln(");");
                output("\tif (lpObject1 != NULL) set");
                output(simpleName);
                output("Fields(env, lpObject1, &lpStruct->");
                output(accessor.getter());
                outputln(");");
                output("\t}");
            }
            outputln();
            if (conditional!=null) {
                outputln("#endif");
            }
        }
        outputln("   return;");
        outputln("}");

    }
    
    private ArrayList<JNIField> getConstantFields(JNIClass clazz) {
        ArrayList<JNIField> rc = new ArrayList<JNIField>();
        List<JNIField> fields = clazz.getDeclaredFields();
        for (JNIField field : fields) {
            int mods = field.getModifiers();
            if ( (mods & Modifier.STATIC) != 0 && field.getFlag(FieldFlag.CONSTANT)) {
                rc.add(field);
            }
        }
        return rc;
    }
    
    public void generate(JNIMethod method) {
        if (method.getFlag(MethodFlag.METHOD_SKIP))
            return;
        
        JNIType returnType = method.getReturnType32(), returnType64 = method.getReturnType64();

        if( method.getFlag(CONSTANT_INITIALIZER)) {
            if( returnType.isType("void") && method.getParameters().isEmpty() ) {
                generateConstantsInitializer(method);
            } else {
                output("#error Warning: invalid CONSTANT_INITIALIZER tagged method. It must be void and take no arguments: ");
                outputln(method.toString());
            }
            return;
        }
        
        if (!(returnType.isType("void") || returnType.isPrimitive() || isSystemClass(returnType) || returnType.isType("java.lang.String"))) {
            output("#error Warning: bad return type. :");
            outputln(method.toString());
            return;
        }
        
        String conditional = method.getConditional();
        if (conditional!=null) {
            outputln("#if "+conditional);
        }
        
        List<JNIParameter> params = method.getParameters();
        String function = getFunctionName(method), function64 = getFunctionName(method, method.getParameterTypes64());
        boolean sameFunction = function.equals(function64);
        if (!sameFunction) {
            output("#ifndef ");
            output(JNI64);
            outputln();
        }
        if (isCPP) {
            output("extern \"C\" ");
            generateFunctionPrototype(method, function, params, returnType, returnType64, true);
            outputln(";");
        }
        if (function.startsWith("CALLBACK_")) {
            generateCallback(method, function, params, returnType);
        }
        generateFunctionPrototype(method, function, params, returnType, returnType64, !sameFunction);
        if (!function.equals(function64)) {
            outputln();
            outputln("#else");
            if (isCPP) {
                output("extern \"C\" ");
                generateFunctionPrototype(method, function64, params, returnType, returnType64, true);
                outputln(";");
            }
            generateFunctionPrototype(method, function64, params, returnType, returnType64, !sameFunction);
            outputln();
            outputln("#endif");
        }
        generateFunctionBody(method, function, function64, params, returnType, returnType64);
        if (conditional!=null) {
            outputln("#endif");
        }
        outputln();
    }

    public void setEnterExitMacro(boolean enterExitMacro) {
        this.enterExitMacro = enterExitMacro;
    }

    void generateNativeMacro(JNIClass clazz) {
        output("#define ");
        output(clazz.getSimpleName());
        output("_NATIVE(func) Java_");
        output(toC(clazz.getName()));
        outputln("_##func");
        outputln();
    }

    boolean generateGetParameter(JNIMethod method, JNIParameter param, boolean critical, int indent) {
        JNIType paramType = param.getType32(), paramType64 = param.getType64();
        if (paramType.isPrimitive() || isSystemClass(paramType))
            return false;
        String iStr = String.valueOf(param.getParameter());
        for (int j = 0; j < indent; j++)
            output("\t");
        output("if (arg");
        output(iStr);
        output(") if ((lparg");
        output(iStr);
        output(" = ");
        if (paramType.isArray()) {
            JNIType componentType = paramType.getComponentType();
            if (componentType.isPrimitive()) {
                if( "long".equals( componentType.getName() ) && param.isPointer() ) {
                    // This case is special as we may need to do pointer conversions..
                    // if your on a 32 bit system but are keeping track of the pointers in a 64 bit long
                    output("hawtjni_malloc_pointer_array(env, arg");
                    output(iStr);
                    output(")");
                } else if (critical) {
                    if (isCPP) {
                        output("(");
                        output(componentType.getTypeSignature2(!paramType.equals(paramType64)));
                        output("*)");
                        output("env->GetPrimitiveArrayCritical(arg");
                    } else {
                        output("(*env)->GetPrimitiveArrayCritical(env, arg");
                    }
                    output(iStr);
                    output(", NULL)");
                } else {
                    if (isCPP) {
                        output("env->Get");
                    } else {
                        output("(*env)->Get");
                    }
                    output(componentType.getTypeSignature1(!paramType.equals(paramType64)));
                    if (isCPP) {
                        output("ArrayElements(arg");
                    } else {
                        output("ArrayElements(env, arg");
                    }
                    output(iStr);
                    output(", NULL)");
                }
            } else {
                throw new Error("not done");
            }
        } else if (paramType.isType("java.lang.String")) {
            if (param.getFlag(ArgFlag.UNICODE)) {
                if (isCPP) {
                    output("env->GetStringChars(arg");
                } else {
                    output("(*env)->GetStringChars(env, arg");
                }
                output(iStr);
                output(", NULL)");
            } else {
                if (isCPP) {
                    output("env->GetStringUTFChars(arg");
                } else {
                    output("(*env)->GetStringUTFChars(env, arg");
                }
                output(iStr);
                output(", NULL)");
            }
        } else {
            if (param.getFlag(ArgFlag.NO_IN)) {
                output("&_arg");
                output(iStr);
            } else {
                output("get");
                output(paramType.getSimpleName());
                output("Fields(env, arg");
                output(iStr);
                output(", &_arg");
                output(iStr);
                output(")");
            }
        }
        outputln(") == NULL) goto fail;");
        return true;
    }

    void generateSetParameter(JNIParameter param, boolean critical) {
        JNIType paramType = param.getType32(), paramType64 = param.getType64();
        if (paramType.isPrimitive() || isSystemClass(paramType))
            return;
        String iStr = String.valueOf(param.getParameter());
        if (paramType.isArray()) {
            output("\tif (arg");
            output(iStr);
            output(" && lparg");
            output(iStr);
            output(") ");
            JNIType componentType = paramType.getComponentType();
            if (componentType.isPrimitive()) {
                if( "long".equals( componentType.getName() ) && param.isPointer() ) {
                    // This case is special as we may need to do pointer conversions..
                    // if your on a 32 bit system but are keeping track of the pointers in a 64 bit long
                    output("hawtjni_free_pointer_array(env, arg");
                    output(iStr);
                } else if (critical) {
                    if (isCPP) {
                        output("env->ReleasePrimitiveArrayCritical(arg");
                    } else {
                        output("(*env)->ReleasePrimitiveArrayCritical(env, arg");
                    }
                    output(iStr);
                } else {
                    if (isCPP) {
                        output("env->Release");
                    } else {
                        output("(*env)->Release");
                    }
                    output(componentType.getTypeSignature1(!paramType.equals(paramType64)));
                    if (isCPP) {
                        output("ArrayElements(arg");
                    } else {
                        output("ArrayElements(env, arg");
                    }
                    output(iStr);
                }
                output(", lparg");
                output(iStr);
                output(", ");
                if (param.getFlag(ArgFlag.NO_OUT)) {
                    output("JNI_ABORT");
                } else {
                    output("0");
                }
                output(");");
            } else {
                throw new Error("not done");
            }
            outputln();
        } else if (paramType.isType("java.lang.String")) {
            output("\tif (arg");
            output(iStr);
            output(" && lparg");
            output(iStr);
            output(") ");
            if (param.getFlag(ArgFlag.UNICODE)) {
                if (isCPP) {
                    output("env->ReleaseStringChars(arg");
                } else {
                    output("(*env)->ReleaseStringChars(env, arg");
                }
            } else {
                if (isCPP) {
                    output("env->ReleaseStringUTFChars(arg");
                } else {
                    output("(*env)->ReleaseStringUTFChars(env, arg");
                }
            }
            output(iStr);
            output(", lparg");
            output(iStr);
            outputln(");");
        } else {
            if (!param.getFlag(ArgFlag.NO_OUT)) {
                output("\tif (arg");
                output(iStr);
                output(" && lparg");
                output(iStr);
                output(") ");
                output("set");
                output(paramType.getSimpleName());
                output("Fields(env, arg");
                output(iStr);
                output(", lparg");
                output(iStr);
                outputln(");");
            }
        }
    }

    void generateEnterExitMacro(JNIMethod method, String function, String function64, boolean enter) {
        if (!enterExitMacro)
            return;
        if (!function.equals(function64)) {
            output("#ifndef ");
            output(JNI64);
            outputln();
        }
        output("\t");
        output(method.getDeclaringClass().getSimpleName());
        output("_NATIVE_");
        output(enter ? "ENTER" : "EXIT");
        output("(env, that, ");
        output(method.getDeclaringClass().getSimpleName()+"_"+function);
        outputln("_FUNC);");
        if (!function.equals(function64)) {
            outputln("#else");
            output("\t");
            output(method.getDeclaringClass().getSimpleName());
            output("_NATIVE_");
            output(enter ? "ENTER" : "EXIT");
            output("(env, that, ");
            output(method.getDeclaringClass().getSimpleName()+"_"+function64);
            outputln("_FUNC);");
            outputln("#endif");
        }
    }

    boolean generateLocalVars(JNIMethod method, List<JNIParameter> params, JNIType returnType, JNIType returnType64) {
        boolean needsReturn = enterExitMacro;
        for (int i = 0; i < params.size(); i++) {
            JNIParameter param = params.get(i);
            JNIType paramType = param.getType32(), paramType64 = param.getType64();
            if (paramType.isPrimitive() || isSystemClass(paramType))
                continue;
            output("\t");
            if (paramType.isArray()) {
                JNIType componentType = paramType.getComponentType();
                if( "long".equals( componentType.getName() ) && param.isPointer() ) {
                    output("void **lparg" + i+"=NULL;");
                } else if (componentType.isPrimitive()) {
                    output(componentType.getTypeSignature2(!paramType.equals(paramType64)));
                    output(" *lparg" + i);
                    output("=NULL;");
                } else {
                    throw new Error("not done");
                }
            } else if (paramType.isType("org.fusesource.hawtjni.runtime.JNIEnv")) {
                // no need to generate a local for this one..
            } else if (paramType.isType("java.lang.String")) {
                if (param.getFlag(ArgFlag.UNICODE)) {
                    output("const jchar *lparg" + i);
                } else {
                    output("const char *lparg" + i);
                }
                output("= NULL;");
            } else {
                if (param.getTypeClass().getFlag(ClassFlag.STRUCT) && !param.getTypeClass().getFlag(ClassFlag.TYPEDEF)) {
                    output("struct ");
                }
                output(paramType.getNativeName());
                output(" _arg" + i);
                if (param.getFlag(ArgFlag.INIT))
                    output("={0}");
                output(", *lparg" + i);
                output("=NULL;");
            }
            outputln();
            needsReturn = true;
        }
        if (needsReturn) {
            if (!returnType.isType("void")) {
                output("\t");
                output(returnType.getTypeSignature2(!returnType.equals(returnType64)));
                outputln(" rc = 0;");
            }
        }
        return needsReturn;
    }

    boolean generateGetters(JNIMethod method, List<JNIParameter> params) {
        boolean genFailTag = false;
        int criticalCount = 0;
        for (JNIParameter param : params) {
            if( !"org.fusesource.hawtjni.runtime.JNIEnv".equals(param.getTypeClass().getName()) ) {
                if (!isCritical(param)) {
                    genFailTag |= generateGetParameter(method, param, false, 1);
                } else {
                    criticalCount++;
                }
            }
        }
        if (criticalCount != 0) {
            outputln("#ifdef JNI_VERSION_1_2");
            outputln("\tif (IS_JNI_1_2) {");
            for (JNIParameter param : params) {
                if( !"org.fusesource.hawtjni.runtime.JNIEnv".equals(param.getTypeClass().getName()) ) {
                    if (isCritical(param)) {
                        genFailTag |= generateGetParameter(method, param, true, 2);
                    }
                }
            }
            outputln("\t} else");
            outputln("#endif");
            outputln("\t{");
            for (JNIParameter param : params) {
                if( !"org.fusesource.hawtjni.runtime.JNIEnv".equals(param.getTypeClass().getName()) ) {
                    if (isCritical(param)) {
                        genFailTag |= generateGetParameter(method, param, false, 2);
                    }
                }
            }
            outputln("\t}");
        }
        return genFailTag;
    }

    void generateSetters(JNIMethod method, List<JNIParameter> params) {
        int criticalCount = 0;
        for (int i = params.size() - 1; i >= 0; i--) {
            JNIParameter param = params.get(i);
            if( !"org.fusesource.hawtjni.runtime.JNIEnv".equals(param.getTypeClass().getName()) ) {
                if (isCritical(param)) {
                    criticalCount++;
                }
            }
        }
        if (criticalCount != 0) {
            outputln("#ifdef JNI_VERSION_1_2");
            outputln("\tif (IS_JNI_1_2) {");
            for (int i = params.size() - 1; i >= 0; i--) {
                JNIParameter param = params.get(i);
                if( !"org.fusesource.hawtjni.runtime.JNIEnv".equals(param.getTypeClass().getName()) ) {
                    if (isCritical(param)) {
                        output("\t");
                        generateSetParameter(param, true);
                    }
                }
            }
            outputln("\t} else");
            outputln("#endif");
            outputln("\t{");
            for (int i = params.size() - 1; i >= 0; i--) {
                JNIParameter param = params.get(i);
                if( !"org.fusesource.hawtjni.runtime.JNIEnv".equals(param.getTypeClass().getName()) ) {
                    if (isCritical(param)) {
                        output("\t");
                        generateSetParameter(param, false);
                    }
                }
            }
            outputln("\t}");
        }
        for (int i = params.size() - 1; i >= 0; i--) {
            JNIParameter param = params.get(i);
            if( !"org.fusesource.hawtjni.runtime.JNIEnv".equals(param.getTypeClass().getName()) ) {
                if (!isCritical(param)) {
                    generateSetParameter(param, false);
                }
            }
        }
    }

    void generateDynamicFunctionCall(JNIMethod method, List<JNIParameter> params, JNIType returnType, JNIType returnType64, boolean needsReturn) {
        outputln("/*");
        generateFunctionCall(method, params, returnType, returnType64, needsReturn);
        outputln("*/");
        outputln("\t{");

        String name = method.getName();
        if (name.startsWith("_"))
            name = name.substring(1);
        output("\t\tLOAD_FUNCTION(fp, ");
        output(name);
        outputln(")");
        outputln("\t\tif (fp) {");
        output("\t\t");
        generateFunctionCallLeftSide(method, returnType, returnType64, needsReturn);
        output("((");
        output(returnType.getTypeSignature2(!returnType.equals(returnType64)));
        output(" (CALLING_CONVENTION*)(");
        for (int i = 0; i < params.size(); i++) {
            if (i != 0)
                output(", ");
            JNIParameter param = params.get(i);
            String cast = param.getCast();
            if( param.isPointer() ) {
                output("(intptr_t)");
            }
            boolean isStruct = param.getFlag(ArgFlag.BY_VALUE);
            if (cast.length() > 2) {
                cast = cast.substring(1, cast.length() - 1);
                if (isStruct) {
                    int index = cast.lastIndexOf('*');
                    if (index != -1)
                        cast = cast.substring(0, index).trim();
                }
                output(cast);
            } else {
                JNIType paramType = param.getType32(), paramType64 = param.getType64();
                output(paramType.getTypeSignature4(!paramType.equals(paramType64), isStruct));
            }
        }
        output("))");
        output("fp");
        output(")");
        generateFunctionCallRightSide(method, params, 0);
        output(";");
        outputln();
        outputln("\t\t}");
        outputln("\t}");
    }

    void generateFunctionCallLeftSide(JNIMethod method, JNIType returnType, JNIType returnType64, boolean needsReturn) {
        output("\t");
        if (!returnType.isType("void")) {
            if (needsReturn) {
                output("rc = ");
            } else {
                output("return ");
            }

            String cast = method.getCast();
            if (cast.length() != 0 && !cast.equals("()")) {
                if( method.isPointer() ) {
                    output("(intptr_t)");
                }
                output(cast);
            } else {
                if( method.getFlag(CPP_NEW)) {
                    String[] parts = getNativeNameParts(method);
                    String className = parts[0];
                    output("(intptr_t)("+className+" *)");
                } else {
                    output("(");
                    output(returnType.getTypeSignature2(!returnType.equals(returnType64)));
                    output(")");
                }
            }
        }
        if (method.getFlag(MethodFlag.ADDRESS)) {
            output("&");
        }
        if (method.getFlag(MethodFlag.JNI)) {
            output(isCPP ? "env->" : "(*env)->");
        }
    }

    void generateFunctionCallRightSide(JNIMethod method, List<JNIParameter> params, int paramStart) {
        if (!method.getFlag(MethodFlag.CONSTANT_GETTER)) {
            output("(");
            if (method.getFlag(MethodFlag.JNI)) {
                if (!isCPP)
                    output("env, ");
            }
            for (int i = paramStart; i < params.size(); i++) {
                JNIParameter param = params.get(i);
                if (i != paramStart)
                    output(", ");
                if (param.getFlag(ArgFlag.BY_VALUE))
                    output("*");
                output(param.getCast());
                if( param.isPointer() ) {
                    output("(intptr_t)");
                }
                if (param.getFlag(ArgFlag.CS_OBJECT))
                    output("TO_OBJECT(");
                if (i == params.size() - 1 && param.getFlag(ArgFlag.SENTINEL)) {
                    output("NULL");
                } else {
                    if( "org.fusesource.hawtjni.runtime.JNIEnv".equals(param.getTypeClass().getName()) ) {
                        output("env");
                    } else {
                        JNIType paramType = param.getType32();
                        if (!paramType.isPrimitive() && !isSystemClass(paramType))
                            output("lp");
                        output("arg" + i);
                    }
                }
                if (param.getFlag(ArgFlag.CS_OBJECT))
                    output(")");
            }
            output(")");
        }
    }

    static String[] getNativeNameParts(JNIMethod method) {
        String className = null;
        String methodName = null;

        JNIClass dc = method.getDeclaringClass();
        if( dc.getFlag(ClassFlag.CPP) || dc.getFlag(ClassFlag.STRUCT) ) {
            className = method.getDeclaringClass().getNativeName();
        }

        if( method.getAccessor().length() != 0 ) {
            methodName = method.getAccessor();
            int pos = methodName.lastIndexOf("::");
            if( pos >= 0 ) {
                className = methodName.substring(0, pos);
                methodName = methodName.substring(pos+2);
            }
        } else {
            methodName = method.getName();
            if( className==null ) {
                int pos = methodName.indexOf("_");
                if( pos > 0 ) {
                    className = methodName.substring(0, pos);
                    methodName = methodName.substring(pos+1);
                }
            }
        }
        if( className==null ) {
            throw new Error(String.format("Could not determine object type name of method '%s'", method.getDeclaringClass().getSimpleName()+"."+method.getName()));
        }
        return new String[]{className, methodName};
    }

    void generateFunctionCall(JNIMethod method, List<JNIParameter> params, JNIType returnType, JNIType returnType64, boolean needsReturn) {
        String name = method.getName();
        String copy = method.getCopy();
        boolean makeCopy = copy.length() != 0 && isCPP && !returnType.isType("void");
        if (makeCopy) {
            output("\t{");
            output("\t\t");
            output(copy);
            output(" temp = ");
        } else {
            generateFunctionCallLeftSide(method, returnType, returnType64, needsReturn);
        }
        int paramStart = 0;
        if (name.startsWith("_"))
            name = name.substring(1);

        boolean objc_struct = false;
        if (name.equals("objc_msgSend_stret") || name.equals("objc_msgSendSuper_stret"))
            objc_struct = true;
        if (objc_struct) {
            outputln("if (sizeof(_arg0) > STRUCT_SIZE_LIMIT) {");
            generate_objc_msgSend_stret(method, params, name);
            paramStart = 1;
        } else if (name.equalsIgnoreCase("call")) {
            output("(");
            JNIParameter param = params.get(0);
            String cast = param.getCast();
            if (cast.length() != 0 && !cast.equals("()")) {
                output(cast);
                if( param.isPointer() ) {
                    output("(intptr_t)");
                }
            } else {
                output("(");
                output(returnType.getTypeSignature2(!returnType.equals(returnType64)));
                output(" (*)())");
            }
            output("arg0)");
            paramStart = 1;
        } else if (name.startsWith("VtblCall") || name.startsWith("_VtblCall")) {
            output("((");
            output(returnType.getTypeSignature2(!returnType.equals(returnType64)));
            output(" (STDMETHODCALLTYPE *)(");
            for (int i = 1; i < params.size(); i++) {
                if (i != 1)
                    output(", ");
                JNIParameter param = params.get(i);
                JNIType paramType = param.getType32(), paramType64 = param.getType64();
                output(paramType.getTypeSignature4(!paramType.equals(paramType64), false));
            }
            output("))(*(");
            JNIType paramType = params.get(1).getType32(), paramType64 = params.get(1).getType64();
            output(paramType.getTypeSignature4(!paramType.equals(paramType64), false));
            output(" **)arg1)[arg0])");
            paramStart = 1;
        } else if (method.getFlag(MethodFlag.CPP_METHOD) || method.getFlag(MethodFlag.SETTER) || method.getFlag(MethodFlag.GETTER) || method.getFlag(MethodFlag.ADDER)) {

            String[] parts = getNativeNameParts(method);
            String className = parts[0];
            String methodName = parts[1];

            if (method.getFlag(MethodFlag.CS_OBJECT)) {
                output("TO_HANDLE(");
            }
            output("(");
            if( params.isEmpty() ) {
                throw new Error(String.format("C++ bound method '%s' missing the 'this' parameter", method.getDeclaringClass().getSimpleName()+"."+method.getName()));
            }
            JNIParameter param = params.get(0);
            if (param.getFlag(ArgFlag.BY_VALUE))
                output("*");
            String cast = param.getCast();
            if (cast.length() != 0 && !cast.equals("()")) {
                output(cast);
                if( param.isPointer() ) {
                    output("(intptr_t)");
                }
            } else {
                output("("+className+" *)(intptr_t)");
            }
            if (param.getFlag(ArgFlag.CS_OBJECT)) {
                output("TO_OBJECT(");
            }
            output("arg0");
            if (param.getFlag(ArgFlag.CS_OBJECT)) {
                output(")");
            }
            output(")->");
            output(methodName);
            paramStart = 1;
        } else if (method.getFlag(MethodFlag.CS_NEW)) {
            output("TO_HANDLE(gcnew ");
            String accessor = method.getAccessor();
            if (accessor.length() != 0) {
                output(accessor);
            } else {
                JNIClass dc = method.getDeclaringClass();
                if( dc.getFlag(ClassFlag.CPP) || dc.getFlag(ClassFlag.STRUCT) ) {
                    output(dc.getNativeName());
                } else {
                    int index = -1;
                    if ((index = name.indexOf('_')) != -1) {
                        output(name.substring(index + 1));
                    } else {
                        output(name);
                    }
                }
            }
        } else if (method.getFlag(MethodFlag.CPP_NEW)) {
            if (method.getFlag(MethodFlag.CS_OBJECT)) {
                output("TO_HANDLE(");
            }
            output("new ");
            String accessor = method.getAccessor();
            if (accessor.length() != 0) {
                output(accessor);
            } else {

                JNIClass dc = method.getDeclaringClass();
                if( dc.getFlag(ClassFlag.CPP) ) {
                    output(method.getDeclaringClass().getNativeName());
                } else {
                    int index = -1;
                    if ((index = name.indexOf('_')) != -1) {
                        output(name.substring(index+1));
                    } else {
                        output(name);
                    }
                }

            }
        } else if (method.getFlag(MethodFlag.CPP_DELETE)) {
            String[] parts = getNativeNameParts(method);
            String className = parts[0];

            output("delete ");
            JNIParameter param = params.get(0);
            String cast = param.getCast();
            if (cast.length() != 0 && !cast.equals("()")) {
                output(cast);
                if( param.isPointer() ) {
                    output("(intptr_t)");
                }
            } else {
                output("("+className+" *)(intptr_t)");
            }
            outputln("arg0;");
            return;
        } else {
            if (method.getFlag(MethodFlag.CS_OBJECT)) {
                output("TO_HANDLE(");
            }
            if (method.getFlag(MethodFlag.CAST)) {
                output("((");
                String returnCast = returnType.getTypeSignature2(!returnType.equals(returnType64));
                if (name.equals("objc_msgSend_bool") && returnCast.equals("jboolean")) {
                    returnCast = "BOOL";
                }
                output(returnCast);
                output(" (*)(");
                for (int i = 0; i < params.size(); i++) {
                    if (i != 0)
                        output(", ");
                    JNIParameter param = params.get(i);
                    String cast = param.getCast();
                    if (cast.length() != 0 && !cast.equals("()") ) {
                        if (cast.startsWith("("))
                            cast = cast.substring(1);
                        if (cast.endsWith(")"))
                            cast = cast.substring(0, cast.length() - 1);
                        output(cast);
                    } else {
                        JNIType paramType = param.getType32(), paramType64 = param.getType64();
                        if (!(paramType.isPrimitive() || paramType.isArray())) {
                            if (param.getTypeClass().getFlag(ClassFlag.STRUCT) && !param.getTypeClass().getFlag(ClassFlag.TYPEDEF)) {
                                output("struct ");
                            }
                        }
                        output(paramType.getTypeSignature4(!paramType.equals(paramType64), param.getFlag(ArgFlag.BY_VALUE)));
                    }
                }
                output("))");
            }
            String accessor = method.getAccessor();
            if (accessor.length() != 0) {
                output(accessor);
            } else {
                output(name);
            }
            if (method.getFlag(MethodFlag.CAST)) {
                output(")");
            }
        }
        if ((method.getFlag(MethodFlag.SETTER) && params.size() == 3) || (method.getFlag(MethodFlag.GETTER) && params.size() == 2)) {
            output("[arg1]");
            paramStart++;
        }
        if (method.getFlag(MethodFlag.SETTER))
            output(" = ");
        if (method.getFlag(MethodFlag.ADDER))
            output(" += ");
        if (!method.getFlag(MethodFlag.GETTER)) {
            generateFunctionCallRightSide(method, params, paramStart);
        }
        if (method.getFlag(MethodFlag.CS_NEW) || method.getFlag(MethodFlag.CS_OBJECT)) {
            output(")");
        }
        output(";");
        outputln();
        if (makeCopy) {
            outputln("\t\t{");
            output("\t\t\t");
            output(copy);
            output("* copy = new ");
            output(copy);
            outputln("();");
            outputln("\t\t\t*copy = temp;");
            output("\t\t\trc = ");
            output("(");
            output(returnType.getTypeSignature2(!returnType.equals(returnType64)));
            output(")");
            outputln("copy;");
            outputln("\t\t}");
            outputln("\t}");
        }
        if (objc_struct) {
            outputln("\t} else {");
            generate_objc_msgSend_stret(method, params, name.substring(0, name.length() - "_stret".length()));
            generateFunctionCallRightSide(method, params, 1);
            outputln(";");
            outputln("\t}");
        }
    }

    void generate_objc_msgSend_stret(JNIMethod method, List<JNIParameter> params, String func) {
        output("\t\t*lparg0 = (*(");
        JNIType paramType = params.get(0).getType32(), paramType64 = params.get(0).getType64();
        output(paramType.getTypeSignature4(!paramType.equals(paramType64), true));
        output(" (*)(");
        for (int i = 1; i < params.size(); i++) {
            if (i != 1)
                output(", ");
            JNIParameter param = params.get(i);
            String cast = param.getCast();
            if( param.isPointer() ) {
                output("(intptr_t)");
            }
            if (cast.length() != 0 && !cast.equals("()")) {
                if (cast.startsWith("("))
                    cast = cast.substring(1);
                if (cast.endsWith(")"))
                    cast = cast.substring(0, cast.length() - 1);
                output(cast);
            } else {
                paramType = param.getType32();
                paramType64 = param.getType64();
                if (!(paramType.isPrimitive() || paramType.isArray())) {
                    if (param.getTypeClass().getFlag(ClassFlag.STRUCT) && !param.getTypeClass().getFlag(ClassFlag.TYPEDEF)) {
                        output("struct ");
                    }
                }
                output(paramType.getTypeSignature4(!paramType.equals(paramType64), param.getFlag(ArgFlag.BY_VALUE)));
            }
        }
        output("))");
        output(func);
        output(")");
    }

    void generateReturn(JNIMethod method, JNIType returnType, boolean needsReturn) {
        if (needsReturn && !returnType.isType("void")) {
            outputln("\treturn rc;");
        }
    }

    void generateMemmove(JNIMethod method, String function, String function64, List<JNIParameter> params) {
        generateEnterExitMacro(method, function, function64, true);
        output("\t");
        boolean get = params.get(0).getType32().isPrimitive();
        String className = params.get(get ? 1 : 0).getType32().getSimpleName();
        output(get ? "if (arg1) get" : "if (arg0) set");
        output(className);
        output(get ? "Fields(env, arg1, (" : "Fields(env, arg0, (");
        output(className);
        output(get ? " *)arg0)" : " *)arg1)");
        outputln(";");
        generateEnterExitMacro(method, function, function64, false);
    }

    void generateFunctionBody(JNIMethod method, String function, String function64, List<JNIParameter> params, JNIType returnType, JNIType returnType64) {
        outputln("{");

        /* Custom GTK memmoves. */
        String name = method.getName();
        if (name.startsWith("_"))
            name = name.substring(1);
        boolean isMemove = (name.equals("memmove") || name.equals("MoveMemory")) && params.size() == 2 && returnType.isType("void");
        if (isMemove) {
            generateMemmove(method, function, function64, params);
        } else {
            boolean needsReturn = generateLocalVars(method, params, returnType, returnType64);
            generateEnterExitMacro(method, function, function64, true);
            boolean genFailTag = generateGetters(method, params);
            if (method.getFlag(MethodFlag.DYNAMIC)) {
                generateDynamicFunctionCall(method, params, returnType, returnType64, needsReturn);
            } else {
                generateFunctionCall(method, params, returnType, returnType64, needsReturn);
            }
            if (genFailTag)
                outputln("fail:");
            generateSetters(method, params);
            generateEnterExitMacro(method, function, function64, false);
            generateReturn(method, returnType, needsReturn);
        }

        outputln("}");
    }

    void generateFunctionPrototype(JNIMethod method, String function, List<JNIParameter> params, JNIType returnType, JNIType returnType64, boolean singleLine) {
        output("JNIEXPORT ");
        output(returnType.getTypeSignature2(!returnType.equals(returnType64)));
        output(" JNICALL ");
        output(method.getDeclaringClass().getSimpleName());
        output("_NATIVE(");
        output(function);
        if (singleLine) {
            output(")");
            output("(JNIEnv *env, ");
        } else {
            outputln(")");
            output("\t(JNIEnv *env, ");
        }
        if ((method.getModifiers() & Modifier.STATIC) != 0) {
            output("jclass");
        } else {
            output("jobject");
        }
        output(" that");
        for (int i = 0; i < params.size(); i++) {
            output(", ");
            JNIType paramType = params.get(i).getType32(), paramType64 = params.get(i).getType64();
            output(paramType.getTypeSignature2(!paramType.equals(paramType64)));
            output(" arg" + i);
        }
        output(")");
        if (!singleLine)
            outputln();
    }

    boolean isCritical(JNIParameter param) {
        JNIType paramType = param.getType32();
        return paramType.isArray() && paramType.getComponentType().isPrimitive() && param.getFlag(ArgFlag.CRITICAL);
    }

    boolean isSystemClass(JNIType type) {
        return type.isType("java.lang.Object") || type.isType("java.lang.Class");
    }

}
