// Run: clang -fsyntax-only -Xclang --ast-print-xml test.c
#include <stdio.h>
#include <stddef.h>
#include <stdarg.h>
#include <stdint.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>
#include <aio.h>
#include <sys/mman.h>

int main () {
    printf ("hello world\n");
}