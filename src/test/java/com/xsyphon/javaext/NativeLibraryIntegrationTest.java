package com.xsyphon.javaext;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for native library functionality.
 * 
 * @author pinealctx
 * @version 1.2.0
 */
class NativeLibraryIntegrationTest {
    
    @Test
    @DisplayName("Test JNI integration")
    void testJNIIntegration() {
        if (TimeX.isJNIAvailable()) {
            System.out.println("Testing JNI integration...");
            
            // Test basic functionality
            long timestamp = TimeX.unixNanoJNI();
            if (timestamp == -1) {
                System.out.println("JNI returned -1, library may not be functional");
                fail("JNI library loaded but returned error (-1)");
            } else {
                assertTrue(timestamp > 0, "JNI should return positive timestamp when functional");
                
                // Test multiple calls
                long[] timestamps = new long[5];
                for (int i = 0; i < timestamps.length; i++) {
                    timestamps[i] = TimeX.unixNanoJNI();
                }
                
                // All should be reasonable (not -1)
                for (long ts : timestamps) {
                    assertTrue(ts > 0, "All JNI timestamps should be positive when functional");
                }
                
                System.out.println("JNI integration test passed");
            }
        } else {
            System.out.println("JNI not available, skipping integration test");
            String error = NativeTimeJNI.getLoadError();
            System.out.println("JNI error: " + error);
            
            // When JNI is not available, calling unixNanoJNI should return -1
            long timestamp = TimeX.unixNanoJNI();
            assertEquals(-1, timestamp, "JNI should return -1 when not available");
        }
    }
    
    @Test
    @DisplayName("Test JNA integration")
    void testJNAIntegration() {
        if (TimeX.isJNAAvailable()) {
            System.out.println("Testing JNA integration...");
            
            // Test basic functionality
            long timestamp = TimeX.unixNanoJNA();
            if (timestamp == -1) {
                System.out.println("JNA returned -1, library may not be functional");
                fail("JNA library loaded but returned error (-1)");
            } else {
                assertTrue(timestamp > 0, "JNA should return positive timestamp when functional");
                
                // Test clock info
                String clockInfo = NativeTimeJNA.getClockInfo();
                assertNotNull(clockInfo, "Clock info should be available");
                System.out.println("Clock info: " + clockInfo);
                
                // Test multiple calls
                long[] timestamps = new long[5];
                for (int i = 0; i < timestamps.length; i++) {
                    timestamps[i] = TimeX.unixNanoJNA();
                }
                
                // All should be reasonable (not -1)
                for (long ts : timestamps) {
                    assertTrue(ts > 0, "All JNA timestamps should be positive when functional");
                }
                
                System.out.println("JNA integration test passed");
            }
        } else {
            System.out.println("JNA not available, skipping integration test");
            String error = NativeTimeJNA.getLoadError();
            System.out.println("JNA error: " + error);
            
            // When JNA is not available, calling unixNanoJNA should return -1
            long timestamp = TimeX.unixNanoJNA();
            assertEquals(-1, timestamp, "JNA should return -1 when not available");
        }
    }
    
    @Test
    @DisplayName("Test fallback behavior")
    void testFallbackBehavior() {
        // These should always work, even if native libraries aren't available
        long instantTime = TimeX.unixNanoInstant();
        long hybridTime = TimeX.unixNanoHybrid();
        long milliTime = TimeX.unixNanoMilliPrecision();
        long optimizedTime = TimeX.unixNanoOptimized();
        
        assertTrue(instantTime > 0, "Instant method should always work");
        assertTrue(hybridTime > 0, "Hybrid method should always work");
        assertTrue(milliTime > 0, "Millisecond precision method should always work");
        assertTrue(optimizedTime > 0, "Optimized method should always work");
        
        // JNI and JNA should either work (positive value) or return -1 (error)
        long jniTime = TimeX.unixNanoJNI();
        long jnaTime = TimeX.unixNanoJNA();
        
        assertTrue(jniTime > 0 || jniTime == -1, "JNI method should return valid timestamp or -1");
        assertTrue(jnaTime > 0 || jnaTime == -1, "JNA method should return valid timestamp or -1");
        
        System.out.println("JNI result: " + (jniTime == -1 ? "not available" : "available"));
        System.out.println("JNA result: " + (jnaTime == -1 ? "not available" : "available"));
        System.out.println("All fallback tests passed");
    }
}
