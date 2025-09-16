#define _POSIX_C_SOURCE 200809L
#include <jni.h>
#include <stdint.h>

// Platform-specific headers
#if __APPLE__
    #include <time.h>
    #include <mach/mach_time.h>
    #include <sys/time.h>
#else
    #include <time.h>
    #include <sys/time.h>
#endif

/* Header for class com_xsyphon_javaext_NativeTimeJNI */
#ifndef _Included_com_xsyphon_javaext_NativeTimeJNI
#define _Included_com_xsyphon_javaext_NativeTimeJNI

#ifdef __cplusplus
extern "C" {
#endif

/*
 * High-performance Unix nanosecond timestamp
 * Optimized for Linux (x86_64/ARM64) and macOS (Intel/ARM64)
 * Returns -1 on failure
 */
JNIEXPORT jlong JNICALL Java_com_xsyphon_javaext_NativeTimeJNI_clockGetTimeNative
  (JNIEnv *env, jclass cls) {
    
#if __APPLE__
    // macOS optimized implementation
    struct timespec ts;
    
    // Try clock_gettime first (macOS 10.12+)
    #ifdef CLOCK_REALTIME
        if (clock_gettime(CLOCK_REALTIME, &ts) == 0) {
            return (jlong)((uint64_t)ts.tv_sec * 1000000000ULL + ts.tv_nsec);
        }
    #endif
    
    // Fallback to gettimeofday (microsecond precision)
    struct timeval tv;
    if (gettimeofday(&tv, NULL) == 0) {
        return (jlong)((uint64_t)tv.tv_sec * 1000000000ULL + tv.tv_usec * 1000ULL);
    }
    
    // Return -1 on failure
    return -1;
    
#else
    // Linux optimized implementation (x86_64/ARM64)
    struct timespec ts;
    
    // Fast path: clock_gettime with CLOCK_REALTIME
    if (clock_gettime(CLOCK_REALTIME, &ts) == 0) {
        return (jlong)((uint64_t)ts.tv_sec * 1000000000ULL + ts.tv_nsec);
    }
    
    // Fallback to gettimeofday
    struct timeval tv;
    if (gettimeofday(&tv, NULL) == 0) {
        return (jlong)((uint64_t)tv.tv_sec * 1000000000ULL + tv.tv_usec * 1000ULL);
    }
    
    // Return -1 on failure
    return -1;
#endif
}

#ifdef __cplusplus
}
#endif

#endif
