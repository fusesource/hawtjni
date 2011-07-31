/*******************************************************************************
 * Copyright (C) 2009-2011 FuseSource Corp.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
#ifndef INCLUDED_PLATFORM_H
#define INCLUDED_PLATFORM_H

#ifdef HAVE_CONFIG_H
  /* configure based build.. we will use what it discovered about the platform */
  #include "config.h"
#else
  #ifdef WIN32
    /* Windows based build */
    #define HAVE_STDLIB_H 1
    #define HAVE_STRINGS_H 1
  #endif
#endif

#ifdef __APPLE__
#import <objc/objc-runtime.h>
#endif

#ifdef HAVE_UNISTD_H
  #include <unistd.h>
#endif

#ifdef HAVE_STDLIB_H
  #include <stdlib.h>
#endif

#ifdef HAVE_STRINGS_H
  #include <string.h>
#endif

#include <fcntl.h>
#include "foo.h"

#include "stdio.h"

class Range {
public:

  int start;
  int end;

  Range() {
    start = 0;
    end = 0;
  }
  Range(const int s, const int e) {
    start = s;
    end = e;
  }

  void dump() {
    printf("range: %d-%d\n", start, end);
  }
};

#endif /* INCLUDED_PLATFORM_H */
