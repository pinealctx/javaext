package com.xsyphon.javaext;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TimeX class methods.
 * Tests correctness, performance characteristics, and platform compatibility.
 * 
 * @author pinealctx
 * @version 1.2.0
 */
@Execution(ExecutionMode.CONCURRENT)
class TimeXTest {
    
    private static final long UNIX_EPOCH_2020 = 1577836800_000_000_000L; // 2020-01-01 00:00:00 UTC in nanoseconds
    private static final long UNIX_EPOCH_2030 = 1893456000_000_000_000L; // 2030-01-01 00:00:00 UTC in nanoseconds
    private static final long TOLERANCE_MS = 1000; // 1 second tolerance for timing tests
    private static final long TOLERANCE_NS = TOLERANCE_MS * 1_000_000L;
    
    @BeforeAll
    static void setUp() {
        System.out.println("Running TimeX tests...");
        System.out.println("Platform: " + TimeX.getPlatformInfo());
        System.out.println("JNI Available: " + TimeX.isJNIAvailable());
        System.out.println("JNA Available: " + TimeX.isJNAAvailable());
    }
    
    @Test
    @DisplayName("Test Instant API method returns reasonable timestamp")
    void testUnixNanoInstant() {
        long timestamp = TimeX.unixNanoInstant();
        
        assertTimestampIsReasonable(timestamp, "Instant API");
        
        // Test multiple calls return increasing values (within tolerance)
        long timestamp2 = TimeX.unixNanoInstant();
        assertTrue(timestamp2 >= timestamp, "Second timestamp should not be earlier than first");
    }
    
    @Test
    @DisplayName("Test System hybrid method returns reasonable timestamp")
    void testUnixNanoHybrid() {
        long timestamp = TimeX.unixNanoHybrid();
        
        assertTimestampIsReasonable(timestamp, "System hybrid");
        
        // Test monotonic behavior
        long timestamp2 = TimeX.unixNanoHybrid();
        assertTrue(timestamp2 >= timestamp, "Hybrid method should be monotonic");
    }
    
    @Test
    @DisplayName("Test millisecond precision method")
    void testUnixNanoMilliPrecision() {
        long timestamp = TimeX.unixNanoMilliPrecision();
        
        assertTimestampIsReasonable(timestamp, "Millisecond precision");
        
        // Check that it's based on milliseconds (last 6 digits should be 000000)
        assertTrue(timestamp % 1_000_000L == 0, 
            "Millisecond precision timestamp should end with 000000");
    }
    
    @Test
    @DisplayName("Test optimized time provider method")
    void testUnixNanoOptimized() {
        long timestamp = TimeX.unixNanoOptimized();
        
        assertTimestampIsReasonable(timestamp, "Optimized provider");
        
        // Test consistency across multiple calls
        long[] timestamps = new long[10];
        for (int i = 0; i < timestamps.length; i++) {
            timestamps[i] = TimeX.unixNanoOptimized();
        }
        
        // All timestamps should be reasonable and generally increasing
        for (int i = 1; i < timestamps.length; i++) {
            assertTimestampIsReasonable(timestamps[i], "Optimized provider (call " + i + ")");
            assertTrue(timestamps[i] >= timestamps[i-1] - TOLERANCE_NS, 
                "Timestamps should be generally monotonic within tolerance");
        }
    }
    
    @Test
    @DisplayName("Test JNI method when available")
    void testUnixNanoJNI() {
        long jniTimestamp = TimeX.unixNanoJNI();
        
        if (!TimeX.isJNIAvailable() || jniTimestamp == -1) {
            System.out.println("JNI not available, should return -1");
            assertEquals(-1, jniTimestamp, "JNI should return -1 when not available");
        } else {
            System.out.println("JNI available, testing native implementation");
            assertTimestampIsReasonable(jniTimestamp, "JNI native");
            
            // Test consistency
            long timestamp2 = TimeX.unixNanoJNI();
            assertTrue(timestamp2 >= jniTimestamp - TOLERANCE_NS, 
                "JNI timestamps should be consistent");
        }
    }
    
    @Test
    @DisplayName("Test JNA method when available")
    void testUnixNanoJNA() {
        long jnaTimestamp = TimeX.unixNanoJNA();
        
        if (!TimeX.isJNAAvailable() || jnaTimestamp == -1) {
            System.out.println("JNA not available, should return -1");
            assertEquals(-1, jnaTimestamp, "JNA should return -1 when not available");
        } else {
            System.out.println("JNA available, testing native implementation");
            assertTimestampIsReasonable(jnaTimestamp, "JNA native");
            
            // Test consistency
            long timestamp2 = TimeX.unixNanoJNA();
            assertTrue(timestamp2 >= jnaTimestamp - TOLERANCE_NS, 
                "JNA timestamps should be consistent");
        }
    }
    
    @Test
    @DisplayName("Test all methods return similar results")
    void testMethodConsistency() {
        long instantTime = TimeX.unixNanoInstant();
        long hybridTime = TimeX.unixNanoHybrid();
        long optimizedTime = TimeX.unixNanoOptimized();
        long jniTime = TimeX.unixNanoJNI();
        long jnaTime = TimeX.unixNanoJNA();
        
        // Collect valid timestamps (excluding -1 error values)
        long[] validTimes = new long[5];
        int validCount = 0;
        
        validTimes[validCount++] = instantTime;
        validTimes[validCount++] = hybridTime;
        validTimes[validCount++] = optimizedTime;
        
        if (jniTime != -1) {
            validTimes[validCount++] = jniTime;
        }
        if (jnaTime != -1) {
            validTimes[validCount++] = jnaTime;
        }
        
        // All valid timestamps should be within a reasonable range of each other
        for (int i = 0; i < validCount; i++) {
            for (int j = i + 1; j < validCount; j++) {
                long diff = Math.abs(validTimes[i] - validTimes[j]);
                assertTrue(diff < TOLERANCE_NS * 2, 
                    String.format("Valid timestamps differ by %d ns (more than %d ns tolerance)", 
                        diff, TOLERANCE_NS * 2));
            }
        }
        
        // At least the Java methods should work
        assertTrue(validCount >= 3, "At least Java-based methods should return valid timestamps");
    }
    
    @ParameterizedTest
    @ValueSource(ints = {1000, 10000, 100000})
    @DisplayName("Test performance characteristics")
    void testPerformanceCharacteristics(int iterations) {
        Supplier<Long>[] methods = new Supplier[]{
            TimeX::unixNanoInstant,
            TimeX::unixNanoHybrid,
            TimeX::unixNanoMilliPrecision,
            TimeX::unixNanoOptimized
        };
        
        String[] methodNames = {
            "Instant", "Hybrid", "MilliPrecision", "Optimized"
        };
        
        for (int i = 0; i < methods.length; i++) {
            long startTime = System.nanoTime();
            
            for (int j = 0; j < iterations; j++) {
                methods[i].get();
            }
            
            long endTime = System.nanoTime();
            long totalTime = endTime - startTime;
            double avgTime = (double) totalTime / iterations;
            
            System.out.printf("Method %s: %d iterations in %.2f ms (avg: %.2f ns/call)%n",
                methodNames[i], iterations, totalTime / 1_000_000.0, avgTime);
            
            // Performance sanity check - should complete within reasonable time
            assertTrue(totalTime < TimeUnit.SECONDS.toNanos(10), 
                methodNames[i] + " method took too long for " + iterations + " iterations");
        }
    }
    
    @Test
    @DisplayName("Test platform information")
    void testPlatformInfo() {
        String platformInfo = TimeX.getPlatformInfo();
        
        assertNotNull(platformInfo, "Platform info should not be null");
        assertFalse(platformInfo.trim().isEmpty(), "Platform info should not be empty");
        assertTrue(platformInfo.contains("OS:"), "Platform info should contain OS information");
        assertTrue(platformInfo.contains("Architecture:"), "Platform info should contain architecture information");
        
        System.out.println("Platform info: " + platformInfo);
    }
    
    @Test
    @DisplayName("Test native library status methods")
    void testNativeLibraryStatus() {
        // Test JNI status
        boolean jniAvailable = TimeX.isJNIAvailable();
        System.out.println("JNI available: " + jniAvailable);
        
        if (!jniAvailable) {
            String jniError = NativeTimeJNI.getLoadError();
            System.out.println("JNI error: " + jniError);
            assertNotNull(jniError, "JNI error message should be provided when not available");
        }
        
        // Test JNA status
        boolean jnaAvailable = TimeX.isJNAAvailable();
        System.out.println("JNA available: " + jnaAvailable);
        
        if (!jnaAvailable) {
            String jnaError = NativeTimeJNA.getLoadError();
            System.out.println("JNA error: " + jnaError);
            assertNotNull(jnaError, "JNA error message should be provided when not available");
        }
        
        if (jnaAvailable) {
            String clockInfo = NativeTimeJNA.getClockInfo();
            System.out.println("Clock info: " + clockInfo);
            assertNotNull(clockInfo, "Clock info should be available when JNA is available");
        }
    }
    
    @Test
    @DisplayName("Test timestamp precision and accuracy")
    void testTimestampPrecision() {
        // Test that different precision methods behave as expected
        long milliPrecision = TimeX.unixNanoMilliPrecision();
        long instant = TimeX.unixNanoInstant();
        
        // Millisecond precision should be less precise
        long milliSeconds = milliPrecision / 1_000_000_000L;
        long instantSeconds = instant / 1_000_000_000L;
        
        // Should be within the same second or very close
        assertTrue(Math.abs(milliSeconds - instantSeconds) <= 1, 
            "Millisecond and instant timestamps should be close in seconds");
        
        // Test nanosecond precision where available
        if (TimeX.isJNAAvailable() || TimeX.isJNIAvailable()) {
            long nativeTime = TimeX.isJNAAvailable() ? TimeX.unixNanoJNA() : TimeX.unixNanoJNI();
            long nativeSeconds = nativeTime / 1_000_000_000L;
            
            assertTrue(Math.abs(nativeSeconds - instantSeconds) <= 1, 
                "Native and instant timestamps should be close in seconds");
        }
    }
    
    /**
     * Helper method to assert that a timestamp is reasonable.
     */
    private void assertTimestampIsReasonable(long timestamp, String methodName) {
        assertTrue(timestamp > 0, methodName + " should return positive timestamp");
        assertTrue(timestamp > UNIX_EPOCH_2020, 
            methodName + " timestamp should be after 2020: " + timestamp);
        assertTrue(timestamp < UNIX_EPOCH_2030, 
            methodName + " timestamp should be before 2030: " + timestamp);
        
        // Compare with system time
        long currentSystemTime = Instant.now().getEpochSecond() * 1_000_000_000L;
        long diff = Math.abs(timestamp - currentSystemTime);
        assertTrue(diff < TOLERANCE_NS, 
            methodName + " timestamp should be close to system time (diff: " + diff + " ns)");
    }
}
