#ifndef INCLUDED_PLATFORM_H
#define INCLUDED_PLATFORM_H

#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>
#include <string.h>

#define PTR_sizeof() sizeof(void *)

#define NATIVE_PTR_CAST void *)(intptr_t
#define JAVA_PTR_CAST   intptr_t)( void *


#endif /* INCLUDED_PLATFORM_H */