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
import java.util.HashMap;
import java.util.List;

import org.fusesource.hawtjni.generator.model.JNIClass;
import org.fusesource.hawtjni.generator.model.JNIField;
import org.fusesource.hawtjni.generator.model.JNIFieldAccessor;
import org.fusesource.hawtjni.generator.model.JNIType;
import org.fusesource.hawtjni.runtime.ClassFlag;

/**
 * 
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class StructsGenerator extends JNIGenerator {

    boolean header;

    static final boolean GLOBAL_REF = false;

    private HashMap<JNIClass, ArrayList<JNIField>> structFields = new HashMap<JNIClass, ArrayList<JNIField>>();

    public StructsGenerator(boolean header) {
        this.header = header;
    }

    public void generateCopyright() {
        outputln(fixDelimiter(getCopyright()));
    }

    public void generateIncludes() {
        if (header) {
            outputln("#include \"" + getOutputName() + ".h\"");
        } else {
            outputln("#include \"" + getOutputName() + ".h\"");
            outputln("#include \"hawtjni.h\"");
            outputln("#include \"" + getOutputName() + "_structs.h\"");
        }
        outputln();
    }

    public void generate(JNIClass clazz) {
        ArrayList<JNIField> fields = getStructFields(clazz);
        if (fields.isEmpty())
            return;
        if (header) {
            generateHeaderFile(clazz);
        } else {
            generateSourceFile(clazz);
        }
    }

    private ArrayList<JNIField> getStructFields(JNIClass clazz) {
        if (!structFields.containsKey(clazz)) {
            ArrayList<JNIField> rc = new ArrayList<JNIField>();
            List<JNIField> fields = clazz.getDeclaredFields();
            for (JNIField field : fields) {
                int mods = field.getModifiers();
                if ((mods & Modifier.STATIC) == 0 && (mods & Modifier.TRANSIENT) == 0) {
                    rc.add(field);
                }
            }

            structFields.put(clazz, rc);
        }
        return structFields.get(clazz);
    }

    void generateHeaderFile(JNIClass clazz) {
        generateSourceStart(clazz);
        generatePrototypes(clazz);
        generateBlankMacros(clazz);
        generateSourceEnd(clazz);
        outputln();
    }

    void generateSourceFile(JNIClass clazz) {
        generateSourceStart(clazz);
        generateFIDsStructure(clazz);
        outputln();
        generateGlobalVar(clazz);
        outputln();
        generateFunctions(clazz);
        generateSourceEnd(clazz);
        outputln();
    }

    void generateSourceStart(JNIClass clazz) {
        String conditional = clazz.getConditional();
        if (conditional != null) {
            outputln("#if " + conditional);
        }
    }

    void generateSourceEnd(JNIClass clazz) {
        if (clazz.getConditional() != null) {
            outputln("#endif");
        }
    }

    void generateGlobalVar(JNIClass clazz) {
        String simpleName = clazz.getSimpleName();
        output(simpleName);
        output("_FID_CACHE ");
        output(simpleName);
        outputln("Fc;");
    }

    void generateBlankMacros(JNIClass clazz) {

        if (clazz.getConditional() == null) {
            return;
        }

        String simpleName = clazz.getSimpleName();
        outputln("#else");
        output("#define cache");
        output(simpleName);
        outputln("Fields(a,b)");
        output("#define get");
        output(simpleName);
        outputln("Fields(a,b,c) NULL");
        output("#define set");
        output(simpleName);
        outputln("Fields(a,b,c)");
    }

    void generatePrototypes(JNIClass clazz) {
        String clazzName = clazz.getNativeName();
        String simpleName = clazz.getSimpleName();
        output("void cache");
        output(simpleName);
        outputln("Fields(JNIEnv *env, jobject lpObject);");
        if (clazz.getFlag(ClassFlag.STRUCT) && !clazz.getFlag(ClassFlag.TYPEDEF)) {
            output("struct ");
        }
        output(clazzName);
        output(" *get");
        output(simpleName);
        output("Fields(JNIEnv *env, jobject lpObject, ");
        if (clazz.getFlag(ClassFlag.STRUCT) && !clazz.getFlag(ClassFlag.TYPEDEF)) {
            output("struct ");
        }
        output(clazzName);
        outputln(" *lpStruct);");
        output("void set");
        output(simpleName);
        output("Fields(JNIEnv *env, jobject lpObject, ");
        if (clazz.getFlag(ClassFlag.STRUCT) && !clazz.getFlag(ClassFlag.TYPEDEF)) {
            output("struct ");
        }
        output(clazzName);
        outputln(" *lpStruct);");
    }

    void generateFIDsStructure(JNIClass clazz) {
        String simpleName = clazz.getSimpleName();
        output("typedef struct ");
        output(simpleName);
        outputln("_FID_CACHE {");
        outputln("\tint cached;");
        outputln("\tjclass clazz;");
        List<JNIField> fields = clazz.getDeclaredFields();
        boolean first = true;
        for (JNIField field : fields) {
            if (ignoreField(field))
                continue;
            if (first)
                output("\tjfieldID ");
            else
                output(", ");
            output(field.getName());
            first = false;
        }
        outputln(";");
        output("} ");
        output(simpleName);
        outputln("_FID_CACHE;");
    }

    void generateCacheFunction(JNIClass clazz) {
        String simpleName = clazz.getSimpleName();
        String clazzName = clazz.getNativeName();
        output("void cache");
        output(simpleName);
        outputln("Fields(JNIEnv *env, jobject lpObject)");
        outputln("{");
        output("\tif (");
        output(simpleName);
        outputln("Fc.cached) return;");
        JNIClass superclazz = clazz.getSuperclass();
        if (!superclazz.getName().equals("java.lang.Object") && hasNonIgnoredFields(superclazz)) {
            String superName = superclazz.getSimpleName();
            output("\tcache");
            output(superName);
            outputln("Fields(env, lpObject);");
        }
        output("\t");
        output(simpleName);
        if (isCPP) {
            if (GLOBAL_REF) {
                output("Fc.clazz = (jclass)env->NewGlobalRef(env->GetObjectClass(lpObject));");
            } else {
                output("Fc.clazz = env->GetObjectClass(lpObject);");
            }
        } else {
            if (GLOBAL_REF) {
                output("Fc.clazz = (*env)->NewGlobalRef(env, (*env)->GetObjectClass(env, lpObject));");
            } else {
                output("Fc.clazz = (*env)->GetObjectClass(env, lpObject);");
            }
        }
        outputln();
        List<JNIField> fields = clazz.getDeclaredFields();
        for (JNIField field : fields) {
            if (ignoreField(field))
                continue;
            output("\t");
            output(simpleName);
            output("Fc.");
            output(field.getName());
            if (isCPP) {
                output(" = env->GetFieldID(");
            } else {
                output(" = (*env)->GetFieldID(env, ");
            }
            output(simpleName);
            output("Fc.clazz, \"");
            output(field.getName());
            JNIType type = field.getType(), type64 = field.getType64();
            output("\", ");
            if (type.equals(type64))
                output("\"");
            output(type.getTypeSignature(!type.equals(type64)));
            if (type.equals(type64))
                output("\"");
            outputln(");");
        }
        // Makes sure compiler/cpu does not reorder the following write before the previous updates are done.
        outputln("\thawtjni_w_barrier();");
        output("\t");
        output(simpleName);
        outputln("Fc.cached = 1;");
        outputln("}");
    }

    void generateGetFields(JNIClass clazz) {
        JNIClass superclazz = clazz.getSuperclass();
        String clazzName = clazz.getNativeName();
        String superName = superclazz.getNativeName();
        String methodname;
        if (!superclazz.getName().equals("java.lang.Object") && hasNonIgnoredFields(superclazz)) {
            /*
             * Windows exception - cannot call get/set function of super class
             * in this case
             */
            if (!(clazzName.equals(superName + "A") || clazzName.equals(superName + "W"))) {
                output("\tget");
                output(superName);
                output("Fields(env, lpObject, (");
                output(superName);
                outputln(" *)lpStruct);");
            } else {
                generateGetFields(superclazz);
            }
        }
        List<JNIField> fields = clazz.getDeclaredFields();
        int sharePtrIndex = 0;
        for (JNIField field : fields) {
            if (ignoreField(field))
                continue;
            String conditional = field.getConditional();
            if (conditional != null) {
                outputln("#if " + conditional);
            }
            JNIType type = field.getType(), type64 = field.getType64();
            String simpleName = type.getSimpleName();
            JNIFieldAccessor accessor = field.getAccessor();
            boolean allowConversion = !type.equals(type64);
            output("\t");
            if (type.isPrimitive()) {
                if (!accessor.isNonMemberSetter())
                    output("lpStruct->");
                if (accessor.isMethodSetter()) {
                    String setterStart = accessor.setter().split("\\(")[0];
                    output(setterStart + "(");
                    if (accessor.isNonMemberSetter())
                        output("lpStruct, ");
                } else {
                    output(accessor.setter());
                    output(" = ");
                }
                if (field.isSharedPointer())
                    output("std::make_shared<" + type.getTypeSignature2(allowConversion) + ">(");
                else
                    output(field.getCast());

                if (field.isPointer()) {
                    output("(intptr_t)");
                }
                if (isCPP) {
                    output("env->Get");
                } else {
                    output("(*env)->Get");
                }
                output(type.getTypeSignature1(!type.equals(type64)));
                if (isCPP) {
                    output("Field(lpObject, ");
                } else {
                    output("Field(env, lpObject, ");
                }
                output(field.getDeclaringClass().getSimpleName());
                output("Fc.");
                output(field.getName());
                if (field.isSharedPointer())
                    output(")");
                if (accessor.isMethodSetter())
                    output(")");
                output(");");
            } else if (type.isArray()) {
                JNIType componentType = type.getComponentType(), componentType64 = type64.getComponentType();
                if (componentType.isPrimitive()) {
                    if (field.isSharedPointer()) {
                        output("(&");
                        output("lpStruct->" + accessor);
                        output("));");
                    }
                    outputln("{");
                    output("\t");
                    output(type.getTypeSignature2(!type.equals(type64)));
                    output(" lpObject1 = (");
                    output(type.getTypeSignature2(!type.equals(type64)));
                    if (isCPP) {
                        output(")env->GetObjectField(lpObject, ");
                    } else {
                        output(")(*env)->GetObjectField(env, lpObject, ");
                    }
                    output(field.getDeclaringClass().getSimpleName());
                    output("Fc.");
                    output(field.getName());
                    outputln(");");
                    if (isCPP) {
                        output("\tenv->Get");
                    } else {
                        output("\t(*env)->Get");
                    }
                    output(componentType.getTypeSignature1(!componentType.equals(componentType64)));
                    if (isCPP) {
                        output("ArrayRegion(lpObject1, 0, sizeof(");
                    } else {
                        output("ArrayRegion(env, lpObject1, 0, sizeof(");
                    }
                    if (!accessor.isNonMemberGetter())
                        output("lpStruct->");
                    output(accessor.getter());
                    output(")");
                    if (!componentType.isType("byte")) {
                        output(" / sizeof(");
                        output(componentType.getTypeSignature2(!componentType.equals(componentType64)));
                        output(")");
                    }
                    output(", (");
                    output(type.getTypeSignature4(!type.equals(type64), false));
                    output(")");
                    if (!accessor.isNonMemberGetter())
                        output("lpStruct->");
                    output(accessor.getter());
                    outputln(");");
                    output("\t}");
                } else {
                    throw new Error("not done");
                }
            } else {
                outputln("\t{");
                if (isCPP) {
                    output("\tjobject lpObject1 = env->GetObjectField(lpObject, ");
                } else {
                    output("\tjobject lpObject1 = (*env)->GetObjectField(env, lpObject, ");
                }
                output(field.getDeclaringClass().getSimpleName());
                output("Fc.");
                output(field.getName());
                outputln(");");
                output("\tif (lpObject1 != NULL) get");
                output(simpleName);
                output("Fields(env, lpObject1, &lpStruct->");
                output(accessor.getter());
                outputln(");");
                output("\t}");
            }
            outputln();
            if (conditional != null) {
                outputln("#endif");
            }
        }
    }

    void generateGetFunction(JNIClass clazz) {
        String clazzName = clazz.getNativeName();
        String simpleName = clazz.getSimpleName();
        if (clazz.getFlag(ClassFlag.STRUCT) && !clazz.getFlag(ClassFlag.TYPEDEF)) {
            output("struct ");
        }
        output(clazzName);
        output(" *get");
        output(simpleName);
        output("Fields(JNIEnv *env, jobject lpObject, ");
        if (clazz.getFlag(ClassFlag.STRUCT) && !clazz.getFlag(ClassFlag.TYPEDEF)) {
            output("struct ");
        }
        output(clazzName);
        outputln(" *lpStruct)");
        outputln("{");
        output("\tif (!");
        output(simpleName);
        output("Fc.cached) cache");
        output(simpleName);
        outputln("Fields(env, lpObject);");
        if (clazz.getFlag(ClassFlag.ZERO_OUT)) {
            outputln("memset(lpStruct, 0, sizeof(struct " + clazzName + "));");
        }
        generateGetFields(clazz);
        outputln("\treturn lpStruct;");
        outputln("}");
    }

    void generateSetFields(JNIClass clazz) {
        JNIClass superclazz = clazz.getSuperclass();
        String clazzName = clazz.getNativeName();
        String superName = superclazz.getNativeName();
        if (!superclazz.getName().equals("java.lang.Object") && hasNonIgnoredFields(superclazz)) {
            /*
             * Windows exception - cannot call get/set function of super class
             * in this case
             */
            if (!(clazzName.equals(superName + "A") || clazzName.equals(superName + "W"))) {
                output("\tset");
                output(superName);
                output("Fields(env, lpObject, (");
                output(superName);
                outputln(" *)lpStruct);");
            } else {
                generateSetFields(superclazz);
            }
        }
        List<JNIField> fields = clazz.getDeclaredFields();
        for (JNIField field : fields) {
            if (ignoreField(field))
                continue;
            String conditional = field.getConditional();
            if (conditional != null) {
                outputln("#if " + conditional);
            }
            JNIType type = field.getType(), type64 = field.getType64();
            boolean allowConversion = !type.equals(type64);

            String simpleName = type.getSimpleName();
            JNIFieldAccessor accessor = field.getAccessor();
            if (type.isPrimitive()) {
                if (isCPP) {
                    output("\tenv->Set");
                } else {
                    output("\t(*env)->Set");
                }
                output(type.getTypeSignature1(allowConversion));
                if (isCPP) {
                    output("Field(lpObject, ");
                } else {
                    output("Field(env, lpObject, ");
                }
                output(field.getDeclaringClass().getSimpleName());
                output("Fc.");
                output(field.getName());
                output(", ");
                output("(" + type.getTypeSignature2(allowConversion) + ")");
                if (field.isPointer()) {
                    output("(intptr_t)");
                }
                if (!accessor.isNonMemberGetter())
                    output("lpStruct->");
                if (accessor.isMethodGetter()) {
                    String getterStart = accessor.getter().split("\\(")[0];
                    output(getterStart + "(");
                    if (accessor.isNonMemberGetter())
                        output("lpStruct");
                    if (field.isSharedPointer())
                        output("->" + field.getName());
                    output(")");
                } else {
                    output(accessor.getter());
                }
                if (field.isSharedPointer()) {
                    output(".get()");
                }
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
                        output(")env->GetObjectField(lpObject, ");
                    } else {
                        output(")(*env)->GetObjectField(env, lpObject, ");
                    }
                    output(field.getDeclaringClass().getSimpleName());
                    output("Fc.");
                    output(field.getName());
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
                    if (!accessor.isNonMemberGetter())
                        output("lpStruct->");
                    if (accessor.isMethodGetter()) {
                        String getterStart = accessor.getter().split("\\(")[0];
                        output(getterStart + "(");
                        if (accessor.isNonMemberGetter())
                            output("lpStruct");
                        output(")");
                    } else {
                        output(accessor.getter());
                    }
                    output(")");
                    if (!componentType.isType("byte")) {
                        output(" / sizeof(");
                        output(componentType.getTypeSignature2(!componentType.equals(componentType64)));
                        output(")");
                    }
                    output(", (");
                    output(type.getTypeSignature4(allowConversion, false));
                    output(")");
                    if (!accessor.isNonMemberGetter())
                        output("lpStruct->");
                    output(accessor.getter());
                    outputln(");");
                    output("\t}");
                } else {
                    throw new Error("not done");
                }
            } else {
                outputln("\t{");
                if (isCPP) {
                    output("\tjobject lpObject1 = env->GetObjectField(lpObject, ");
                } else {
                    output("\tjobject lpObject1 = (*env)->GetObjectField(env, lpObject, ");
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
            if (conditional != null) {
                outputln("#endif");
            }
        }
    }

    void generateSetFunction(JNIClass clazz) {
        String clazzName = clazz.getNativeName();
        String simpleName = clazz.getSimpleName();
        output("void set");
        output(simpleName);
        output("Fields(JNIEnv *env, jobject lpObject, ");
        if (clazz.getFlag(ClassFlag.STRUCT) && !clazz.getFlag(ClassFlag.TYPEDEF)) {
            output("struct ");
        }
        output(clazzName);
        outputln(" *lpStruct)");
        outputln("{");
        output("\tif (!");
        output(simpleName);
        output("Fc.cached) cache");
        output(simpleName);
        outputln("Fields(env, lpObject);");
        generateSetFields(clazz);
        outputln("}");
    }

    void generateFunctions(JNIClass clazz) {
        generateCacheFunction(clazz);
        outputln();
        generateGetFunction(clazz);
        outputln();
        generateSetFunction(clazz);
    }

    boolean ignoreField(JNIField field) {
        int mods = field.getModifiers();
        return field.ignore() || ((mods & Modifier.FINAL) != 0) || ((mods & Modifier.STATIC) != 0);
    }

    boolean hasNonIgnoredFields(JNIClass clazz) {
        for (JNIField field : getStructFields(clazz))
            if (!ignoreField(field)) return true;
        return false;
    }

}

