/*******************************************************************************
 * Copyright (c) 2009 Progress Software, Inc.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
#ifndef INCLUDED_FOO_H
#define INCLUDED_FOO_H

#include <stdlib.h>

struct foo {
   int    a;
   size_t b;     
   char   c[20];        
   struct foo *prev;            	
};

void print_foo(struct foo *arg);
long foowork(struct foo **arg, int count);

#endif /* INCLUDED_FOO_H */