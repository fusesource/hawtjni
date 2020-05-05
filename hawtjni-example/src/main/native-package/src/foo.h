/*******************************************************************************
 * Copyright (C) 2009-2011 FuseSource Corp.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
#ifndef INCLUDED_FOO_H
#define INCLUDED_FOO_H

#include <stdlib.h>
#include <memory>
#include "jni.h"

#ifdef __cplusplus
extern "C" {
#endif

struct foo {
   int    a;
   size_t b;
   char   c[20];
   struct foo *prev;
   long CheckStr;
};

typedef struct _point {
   int    x;
   int    y;
} point;

struct ClassWithAccessors {
    float e;

    float (*get_e)();
    void (*set_e)(float e);
};

float get_d(struct foo *arg);
void set_d(struct foo *arg, float d);

float ClassWithAccessors_get_e(struct foo *arg);
void ClassWithAccessors_set_e(struct foo *arg, float e);

struct foo * foo_add(struct foo *arg, int count);
char * char_add(char *arg, int count);

void print_foo(struct foo *arg);
long foowork(struct foo **arg, int count);

void callmeback(void (*thecallback)(int number));

void passingtheenv (const char *who, JNIEnv *env);

#ifdef __cplusplus
} /* extern "C" */
#endif

std::shared_ptr<intptr_t> get_sp(long CheckStr);
void set_sp(struct foo *arg, std::shared_ptr<intptr_t>);

#endif /* INCLUDED_FOO_H */
