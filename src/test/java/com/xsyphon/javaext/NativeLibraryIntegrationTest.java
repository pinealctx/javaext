package com.xsyphon.javaext;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for native library functionality.
 * 
 * @author pinealctx
 * @version 1.0.0
 */
class NativeLibraryIntegrationTest {
    
    @Test
    @DisplayName("Test JNI integration")
    void testJNIIntegration() {
        if (TimeX.isJNIAvailable()) {
            System.out.println("Testing JNI integration...");
            
            // Test basic functionality
            long timestamp = TimeX.unixNanoJNI();
            assertTrue(timestamp > 0, "JNI should return positive timestamp");
            
            // Test multiple calls
            long[] timestamps = new long[5];
            for (int i = 0; i < timestamps.length; i++) {
                timestamps[i] = TimeX.unixNanoJNI();
            }
            
            // All should be reasonable
            for (long ts : timestamps) {
                assertTrue(ts > 0, "All JNI timestamps should be positive");
            }
            
            System.out.println("JNI integration test passed");
        } else {
            System.out.println("JNI not available, skipping integration test");
            String error = NativeTimeJNI.getLoadError();
            System.out.println("JNI error: " + error);
        }
    }
    
    @Test
    @DisplayName("Test JNA integration")
    void testJNAIntegration() {
        if (TimeX.isJNAAvailable()) {
            System.out.println("Testing JNA integration...");
            
            // Test basic functionality
            long timestamp = TimeX.unixNanoJNA();
            assertTrue(timestamp > 0, "JNA should return positive timestamp");
            
            // Test clock info
            String clockInfo = NativeTimeJNA.getClockInfo();
            assertNotNull(clockInfo, "Clock info should be available");
            System.out.println("Clock info: " + clockInfo);
            
            // Test multiple calls
            long[] timestamps = new long[5];
            for (int i = 0; i < timestamps.length; i++) {
                timestamps[i] = TimeX.unixNanoJNA();
            }
            
            // All should be reasonable
            for (long ts : timestamps) {
                assertTrue(ts > 0, "All JNA timestamps should be positive");
            }
            
            System.out.println("JNA integration test passed");
        } else {
            System.out.println("JNA not available, skipping integration test");
            String error = NativeTimeJNA.getLoadError();
            System.out.println("JNA error: " + error);
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
        
        // JNI and JNA should fall back gracefully
        long jniTime = TimeX.unixNanoJNI();
        long jnaTime = TimeX.unixNanoJNA();
        
        assertTrue(jniTime > 0, "JNI method should work (native or fallback)");
        assertTrue(jnaTime > 0, "JNA method should work (native or fallback)");
        
        System.out.println("All fallback tests passed");
    }
}
