package com.xsyphon.javaext;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

/**
 * JNA interface for native time functions.
 * Provides cross-platform access to high-resolution time functions
 * using Java Native Access (JNA).
 * 
 * @author pinealctx
 * @version 1.2.0
 */
public class NativeTimeJNA {
    
    private static CLibrary cLibrary = null;
    private static boolean available = false;
    private static String loadError = null;
    
    // Clock types
    private static final int CLOCK_REALTIME = 0;
    private static final int CLOCK_MONOTONIC = 1;
    
    static {
        try {
            initializeLibrary();
            available = true;
        } catch (Exception e) {
            loadError = e.getMessage();
            available = false;
        }
    }
    
    /**
     * Initialize the native library interface.
     * 
     * @throws Exception if initialization fails
     */
    private static void initializeLibrary() throws Exception {
        try {
            if (Platform.isLinux() || Platform.isMac()) {
                cLibrary = Native.load("c", CLibrary.class);
                
                // Test the library by making a test call
                TimeSpec testTime = new TimeSpec();
                int result = cLibrary.clock_gettime(CLOCK_REALTIME, testTime);
                if (result != 0) {
                    throw new RuntimeException("clock_gettime test call failed with result: " + result);
                }
            } else {
                throw new UnsupportedOperationException("Platform not supported: " + Platform.getOSType());
            }
        } catch (Exception e) {
            throw new Exception("Failed to initialize JNA library: " + e.getMessage(), e);
        }
    }
    
    /**
     * Check if JNA native library interface is available.
     * 
     * @return true if available
     */
    public static boolean isAvailable() {
        return available;
    }
    
    /**
     * Get the error message if library loading failed.
     * 
     * @return error message or null if no error
     */
    public static String getLoadError() {
        return loadError;
    }
    
    /**
     * Get Unix nanosecond timestamp using JNA clock_gettime call.
     * 
     * @return Unix nanosecond timestamp, or -1 if call fails
     */
    public static long clockGetTime() {
        if (!available) {
            return -1; // JNA library not available
        }
        
        TimeSpec timeSpec = new TimeSpec();
        int result = cLibrary.clock_gettime(CLOCK_REALTIME, timeSpec);
        
        if (result != 0) {
            return -1; // clock_gettime failed
        }
        
        return timeSpec.tv_sec * 1_000_000_000L + timeSpec.tv_nsec;
    }
    
    /**
     * Get monotonic nanosecond timestamp using JNA clock_gettime call.
     * This provides a monotonic clock that is not affected by system time changes.
     * 
     * @return monotonic nanosecond timestamp, or -1 if call fails
     */
    public static long clockGetTimeMonotonic() {
        if (!available) {
            return -1; // JNA library not available
        }
        
        TimeSpec timeSpec = new TimeSpec();
        int result = cLibrary.clock_gettime(CLOCK_MONOTONIC, timeSpec);
        
        if (result != 0) {
            return -1; // clock_gettime failed
        }
        
        return timeSpec.tv_sec * 1_000_000_000L + timeSpec.tv_nsec;
    }
    
    /**
     * C library interface for JNA.
     */
    public interface CLibrary extends Library {
        /**
         * Get time from specified clock.
         * 
         * @param clockId clock identifier
         * @param timeSpec time specification structure
         * @return 0 on success, -1 on error
         */
        int clock_gettime(int clockId, TimeSpec timeSpec);
    }
    
    /**
     * Time specification structure matching the C struct timespec.
     */
    public static class TimeSpec extends Structure {
        /** Seconds */
        public long tv_sec;
        /** Nanoseconds */
        public long tv_nsec;
        
        public TimeSpec() {
            super();
        }
        
        public TimeSpec(long sec, long nsec) {
            super();
            this.tv_sec = sec;
            this.tv_nsec = nsec;
        }
        
        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("tv_sec", "tv_nsec");
        }
        
        @Override
        public String toString() {
            return String.format("TimeSpec{tv_sec=%d, tv_nsec=%d}", tv_sec, tv_nsec);
        }
    }
    
    /**
     * Get platform-specific information about clock resolution.
     * 
     * @return platform clock information
     */
    public static String getClockInfo() {
        if (!available) {
            return "JNA not available: " + loadError;
        }
        
        try {
            TimeSpec realtime = new TimeSpec();
            TimeSpec monotonic = new TimeSpec();
            
            int realtimeResult = cLibrary.clock_gettime(CLOCK_REALTIME, realtime);
            int monotonicResult = cLibrary.clock_gettime(CLOCK_MONOTONIC, monotonic);
            
            return String.format(
                "Platform: %s, CLOCK_REALTIME: %s (result=%d), CLOCK_MONOTONIC: %s (result=%d)",
                Platform.getOSType(),
                realtimeResult == 0 ? realtime.toString() : "failed",
                realtimeResult,
                monotonicResult == 0 ? monotonic.toString() : "failed",
                monotonicResult
            );
        } catch (Exception e) {
            return "Error getting clock info: " + e.getMessage();
        }
    }
}
