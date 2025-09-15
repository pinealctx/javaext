#include <jni.h>
#include <stdint.h>
#if defined(__APPLE__)
#include <mach/mach_time.h>
#include <time.h>
#else
#include <time.h>
#endif

JNIEXPORT jlong JNICALL Java_com_xsyphon_javaext_TimeXJni_unixNanoByJni(JNIEnv *env, jclass clazz) {
#if defined(__APPLE__)
    struct timespec ts;
    clock_gettime(CLOCK_REALTIME, &ts);
    return (jlong)ts.tv_sec * 1000000000LL + ts.tv_nsec;
#else
    struct timespec ts;
    clock_gettime(CLOCK_REALTIME, &ts);
    return (jlong)ts.tv_sec * 1000000000LL + ts.tv_nsec;
#endif
}
