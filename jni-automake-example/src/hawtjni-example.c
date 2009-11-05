
#include "hawtjni.h"
#include "hawtjni-example_structs.h"
#include "hawtjni-example_stats.h"

#define Platform_NATIVE(func) Java_test_Platform_##func

#ifndef NO_free
JNIEXPORT void JNICALL Platform_NATIVE(free)
	(JNIEnv *env, jclass that, jlong arg0)
{
	Platform_NATIVE_ENTER(env, that, free_FUNC);
	free((void *)arg0);
	Platform_NATIVE_EXIT(env, that, free_FUNC);
}
#endif

#ifndef NO_malloc
JNIEXPORT jlong JNICALL Platform_NATIVE(malloc)
	(JNIEnv *env, jclass that, jlong arg0)
{
	jlong rc = 0;
	Platform_NATIVE_ENTER(env, that, malloc_FUNC);
	rc = (intptr_t)malloc(arg0);
	Platform_NATIVE_EXIT(env, that, malloc_FUNC);
	return rc;
}
#endif

#ifndef NO_open
JNIEXPORT jlong JNICALL Platform_NATIVE(open)
	(JNIEnv *env, jclass that, jstring arg0, jint arg1, jint arg2)
{
	const char *lparg0= NULL;
	jlong rc = 0;
	Platform_NATIVE_ENTER(env, that, open_FUNC);
	if (arg0) if ((lparg0 = (*env)->GetStringUTFChars(env, arg0, NULL)) == NULL) goto fail;
	rc = open(lparg0, arg1, arg2);
fail:
	if (arg0 && lparg0) (*env)->ReleaseStringUTFChars(env, arg0, lparg0);
	Platform_NATIVE_EXIT(env, that, open_FUNC);
	return rc;
}
#endif

