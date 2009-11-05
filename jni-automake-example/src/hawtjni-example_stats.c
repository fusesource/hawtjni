
#include "hawtjni.h"
#include "hawtjni-example_stats.h"

#ifdef NATIVE_STATS

int Platform_nativeFunctionCount = 3;
int Platform_nativeFunctionCallCount[3];
char * Platform_nativeFunctionNames[] = {
	"free",
	"malloc",
	"open",
};

#define STATS_NATIVE(func) Java_org_fusesource_hawtjni_runtime_NativeStats_##func

JNIEXPORT jint JNICALL STATS_NATIVE(Platform_1GetFunctionCount)
	(JNIEnv *env, jclass that)
{
	return Platform_nativeFunctionCount;
}

JNIEXPORT jstring JNICALL STATS_NATIVE(Platform_1GetFunctionName)
	(JNIEnv *env, jclass that, jint index)
{
	return (*env)->NewStringUTF(env, Platform_nativeFunctionNames[index]);
}

JNIEXPORT jint JNICALL STATS_NATIVE(Platform_1GetFunctionCallCount)
	(JNIEnv *env, jclass that, jint index)
{
	return Platform_nativeFunctionCallCount[index];
}

#endif
