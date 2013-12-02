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
#include <string.h>
#include <stdio.h>

int IS_JNI_1_2 = 0;

#ifdef HAVE_PTHREAD_H
  #include <pthread.h>
#endif

#ifdef HAVE_UNISTD_H
  #include <unistd.h>
#endif

#ifdef JNI_VERSION_1_2
JavaVM *JVM;

#ifdef HAVE_PTHREAD_H
pthread_key_t JNI_ATTACH_THREAD_LOCAL_KEY;
#endif

jint hawtjni_attach_thread(JNIEnv **env, const char *thread_name) {
  JavaVMAttachArgs args;
  args.version = JNI_VERSION_1_2;
  args.name = (char *)thread_name;
  args.group = 0;
  #ifdef HAVE_PTHREAD_H
    if( JNI_ATTACH_THREAD_LOCAL_KEY ) {
      *env = pthread_getspecific(JNI_ATTACH_THREAD_LOCAL_KEY);
      if( ! *env ) {
        if( (*JVM)->AttachCurrentThread(JVM, (void**)env, &args)==0 ) {
          pthread_setspecific(JNI_ATTACH_THREAD_LOCAL_KEY, *env);
        } else {
          return -1;
        }
      }
      return 0;
    } else {
      return (*JVM)->AttachCurrentThread(JVM, (void**)env, &args);
    }
  #else
    return (*JVM)->AttachCurrentThread(JVM, (void**)env, &args);
  #endif
}

jint hawtjni_detach_thread() {
  #ifdef HAVE_PTHREAD_H
    if( JNI_ATTACH_THREAD_LOCAL_KEY ) {
      // Don't actually detach.. that will automatically
      // happen when the the thread dies.
      return 0;
    } else {
      return (*JVM)->DetachCurrentThread(JVM);
    }
  #else
    return (*JVM)->DetachCurrentThread(JVM);
  #endif
}

#ifdef HAVE_PTHREAD_H
  void hawtjni_thread_cleanup(void *data) {
    if( data ) {
      (*JVM)->DetachCurrentThread(JVM);
      pthread_setspecific(JNI_ATTACH_THREAD_LOCAL_KEY, 0);
    }
  }
#endif

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
  IS_JNI_1_2 = 1;
  JVM = vm;
  #ifdef HAVE_PTHREAD_H
    if( pthread_key_create(&JNI_ATTACH_THREAD_LOCAL_KEY, hawtjni_thread_cleanup) ) {
      JNI_ATTACH_THREAD_LOCAL_KEY = 0;
    }
  #endif
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
