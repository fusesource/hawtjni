
#ifdef NATIVE_STATS
extern int Platform_nativeFunctionCount;
extern int Platform_nativeFunctionCallCount[];
extern char* Platform_nativeFunctionNames[];
#define Platform_NATIVE_ENTER(env, that, func) Platform_nativeFunctionCallCount[func]++;
#define Platform_NATIVE_EXIT(env, that, func) 
#else
#ifndef Platform_NATIVE_ENTER
#define Platform_NATIVE_ENTER(env, that, func) 
#endif
#ifndef Platform_NATIVE_EXIT
#define Platform_NATIVE_EXIT(env, that, func) 
#endif
#endif

typedef enum {
	free_FUNC,
	malloc_FUNC,
	open_FUNC,
} Platform_FUNCS;
