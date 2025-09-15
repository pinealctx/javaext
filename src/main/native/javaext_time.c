#define _POSIX_C_SOURCE 200809L
#include <jni.h>
#include <time.h>
#include <errno.h>
#include <string.h>
#include <stdio.h>

/* Header for class com_xsyphon_javaext_NativeTimeJNI */
#ifndef _Included_com_xsyphon_javaext_NativeTimeJNI
#define _Included_com_xsyphon_javaext_NativeTimeJNI

#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     com_xsyphon_javaext_NativeTimeJNI
 * Method:    clockGetTimeNative
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_xsyphon_javaext_NativeTimeJNI_clockGetTimeNative
  (JNIEnv *env, jclass cls) {
    
    struct timespec ts;
    
    // Use clock_gettime with CLOCK_REALTIME for Unix timestamp
    if (clock_gettime(CLOCK_REALTIME, &ts) != 0) {
        // Throw RuntimeException if clock_gettime fails
        jclass exceptionClass = (*env)->FindClass(env, "java/lang/RuntimeException");
        if (exceptionClass != NULL) {
            char errorMsg[256];
            snprintf(errorMsg, sizeof(errorMsg), "clock_gettime failed: %s", strerror(errno));
            (*env)->ThrowNew(env, exceptionClass, errorMsg);
        }
        return 0;
    }
    
    // Convert to nanoseconds since Unix epoch
    jlong nanos = (jlong)ts.tv_sec * 1000000000LL + (jlong)ts.tv_nsec;
    return nanos;
}

#ifdef __cplusplus
}
#endif

#endif
