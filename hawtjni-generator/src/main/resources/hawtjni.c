/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/* == HEADER-SNIP-LOCATION == */ 
#include "hawtjni.h"
#include <stdlib.h>

int IS_JNI_1_2 = 0;

#ifdef JNI_VERSION_1_2
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
	IS_JNI_1_2 = 1;
	return JNI_VERSION_1_2;
}
#endif

void throwOutOfMemory(JNIEnv *env) {
	jclass clazz = (*env)->FindClass(env, "java/lang/OutOfMemoryError");
	if (clazz != NULL) {
		(*env)->ThrowNew(env, clazz, "");
	}
}

#ifndef JNI64

void **hawtjni_malloc_pointer_array(JNIEnv *env, jlongArray array) {
  int i, size;
  jlong *elems;
  void **rc;
  
  if( array==NULL ) {
    return NULL;
  }
#ifdef JNI_VERSION_1_2
	if (IS_JNI_1_2) {
    elems = (*env)->GetPrimitiveArrayCritical(env, array, NULL);
	} else
#endif
	{
    elems = (*env)->GetLongArrayElements(env, array, NULL);
	}	
  if( elems == NULL) {
    return NULL;
  }
  
  size = (*env)->GetArrayLength(env, array);
  rc=malloc(sizeof(void *)*(size+1));
  if( rc!= NULL ) {
    for( i=0; i < size; i++ ) {
      rc[i]=(void *)(intptr_t)(elems[i]);
    }
    rc[size]=NULL;
  }
#ifdef JNI_VERSION_1_2
	if (IS_JNI_1_2) {
	  (*env)->ReleasePrimitiveArrayCritical(env, array, elems, JNI_ABORT);
	} else
#endif
	{
    (*env)->ReleaseLongArrayElements(env, array, elems, JNI_ABORT);
	}
  return rc;
}

void hawtjni_free_pointer_array(JNIEnv *env, jlongArray array, void **elems, jint mode) {

	// do we need to copy back the data??
	if( mode != JNI_ABORT) {
		int i, size;
    jlong *tmp;

	  size = (*env)->GetArrayLength(env, array);
#ifdef JNI_VERSION_1_2
		if (IS_JNI_1_2) {
	    tmp = (*env)->GetPrimitiveArrayCritical(env, array, NULL);
		} else
#endif
		{
	    tmp = (*env)->GetLongArrayElements(env, array, NULL);
		}	
	  if( tmp != NULL) { 
	    for( i=0; i < size; i++ ) {
	      tmp[i]=(intptr_t)elems[i];
	    }
#ifdef JNI_VERSION_1_2
  		if (IS_JNI_1_2) {
  		  (*env)->ReleasePrimitiveArrayCritical(env, array, tmp, 0);
  		} else
#endif
  		{
  	    (*env)->ReleaseLongArrayElements(env, array, tmp, 0);
  		}
	  }
	} /* mode != JNI_ABORTmode */
	
  free(elems);	
}

#endif /* JNI64 */