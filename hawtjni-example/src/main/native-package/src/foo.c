#include "foo.h"
#include <stdio.h>

void print_foo(struct foo *arg) {
   printf("foo@%p: { a: %d, b: %zd, c: \"%s\", prev: @%p}\n", arg, arg->a, arg->b, arg->c, arg->prev); 
}

long foowork(struct foo **arg, int count) {
    long rc=0;
	int i=0;
	for( i=0; i < count; i++ ) {
	   rc = rc + (*arg)->a;
	   rc = rc + (*arg)->b;
	   arg++;
	}
	return rc;
}