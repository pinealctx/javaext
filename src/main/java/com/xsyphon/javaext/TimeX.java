package com.xsyphon.javaext;

import java.time.Instant;

/**
 * High-performance Unix nanosecond timestamp utilities.
 * Provides multiple methods for obtaining Unix nanosecond timestamps
 * with different performance characteristics and platform compatibility.
 * 
 * @author pinealctx
 * @version 1.1.0
 */
public class TimeX {
    
    // Constants for time conversions
    private static final long NANOS_PER_SECOND = 1_000_000_000L;
    private static final long NANOS_PER_MILLI = 1_000_000L;
    private static final int MILLIS_PER_SECOND = 1000;
    
    // System nanosecond time offset for hybrid method
    private static final long SYSTEM_NANOTIME_OFFSET;
    
    // Initialize the system nanosecond offset at class loading time
    static {
        long currentTimeMillis = System.currentTimeMillis();
        long nanoTime = System.nanoTime();
        SYSTEM_NANOTIME_OFFSET = currentTimeMillis * NANOS_PER_MILLI - nanoTime;
    }
    
    // Optimized time provider instance
    private static final OptimizedTimeProvider TIME_PROVIDER = new OptimizedTimeProvider();
    
    /**
     * Get Unix nanosecond timestamp using standard Java Instant API.
     * This method provides good accuracy but may have higher overhead.
     * 
     * @return Unix nanosecond timestamp
     */
    public static long unixNanoInstant() {
        Instant now = Instant.now();
        return now.getEpochSecond() * NANOS_PER_SECOND + now.getNano();
    }
    
    /**
     * Get Unix nanosecond timestamp using hybrid System time methods.
     * This method combines System.currentTimeMillis() with System.nanoTime()
     * for better performance while maintaining reasonable accuracy.
     * 
     * @return Unix nanosecond timestamp
     */
    public static long unixNanoHybrid() {
        return System.nanoTime() + SYSTEM_NANOTIME_OFFSET;
    }
    
    /**
     * Get Unix nanosecond timestamp using JNI native clock_gettime call.
     * Falls back to Instant method on unsupported platforms.
     * 
     * @return Unix nanosecond timestamp
     */
    public static long unixNanoJNI() {
        try {
            return NativeTimeJNI.clockGetTime();
        } catch (Exception e) {
            // Fallback to Instant method on error
            return unixNanoInstant();
        }
    }
    
    /**
     * Get Unix nanosecond timestamp using JNA native library call.
     * Falls back to Instant method on unsupported platforms.
     * 
     * @return Unix nanosecond timestamp
     */
    public static long unixNanoJNA() {
        try {
            return NativeTimeJNA.clockGetTime();
        } catch (Exception e) {
            // Fallback to Instant method on error
            return unixNanoInstant();
        }
    }
    
    /**
     * Get Unix nanosecond timestamp using optimized currentTimeMillis
     * with millisecond precision only.
     * This is the fastest method but with limited precision.
     * 
     * @return Unix nanosecond timestamp (millisecond precision)
     */
    public static long unixNanoMilliPrecision() {
        return System.currentTimeMillis() * NANOS_PER_MILLI;
    }
    
    /**
     * Get Unix nanosecond timestamp using optimized time provider algorithm.
     * This method attempts to provide nanosecond precision while maintaining
     * high performance through clever calibration.
     * 
     * @return Unix nanosecond timestamp
     */
    public static long unixNanoOptimized() {
        return TIME_PROVIDER.currentTimeNanos();
    }
    
    /**
     * Get the current platform information.
     * 
     * @return Platform information string
     */
    public static String getPlatformInfo() {
        String os = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch").toLowerCase();
        return String.format("OS: %s, Architecture: %s", os, arch);
    }
    
    /**
     * Check if JNI native libraries are available.
     * 
     * @return true if JNI libraries are loaded and functional
     */
    public static boolean isJNIAvailable() {
        return NativeTimeJNI.isAvailable();
    }
    
    /**
     * Check if JNA native libraries are available.
     * 
     * @return true if JNA libraries are loaded and functional
     */
    public static boolean isJNAAvailable() {
        return NativeTimeJNA.isAvailable();
    }
    
    /**
     * Optimized time provider based on the algorithm from the task description.
     * This implementation attempts to provide high-precision timestamps
     * while maintaining excellent performance characteristics.
     */
    private static class OptimizedTimeProvider {
        private long delta = 0L;
        
        /**
         * Get current time in milliseconds.
         * 
         * @return current time in milliseconds
         */
        public long currentTimeMillis() {
            return System.currentTimeMillis();
        }
        
        /**
         * Get current time in microseconds.
         * 
         * @return current time in microseconds
         */
        public long currentTimeMicros() {
            return this.currentTimeNanos() / 1000L;
        }
        
        /**
         * Get current time in nanoseconds with optimization.
         * This method balances precision with performance by using
         * a delta correction mechanism.
         * 
         * @return current time in nanoseconds
         */
        public long currentTimeNanos() {
            long nowNS = System.nanoTime();
            long nowMS = this.currentTimeMillis() * NANOS_PER_MILLI;
            long estimate = nowNS + this.delta;
            
            if (estimate < nowMS) {
                this.delta = nowMS - nowNS;
                return nowMS;
            } else if (estimate > nowMS + NANOS_PER_MILLI) {
                nowMS += NANOS_PER_MILLI;
                this.delta = nowMS - nowNS;
                return nowMS;
            } else {
                return estimate;
            }
        }
    }
    
    // Static initialization block for optimized time provider calibration
    static {
        // Warm up the optimized time provider for 5 milliseconds
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() < start + 5L) {
            TIME_PROVIDER.currentTimeNanos();
            // Small pause to prevent busy waiting
            try {
                Thread.onSpinWait(); // Java 9+ optimization hint
            } catch (Exception ignored) {
                // Fallback for older Java versions
            }
        }
    }
}
