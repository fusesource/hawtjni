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