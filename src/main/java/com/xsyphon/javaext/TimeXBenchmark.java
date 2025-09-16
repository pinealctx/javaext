package com.xsyphon.javaext;

/**
 * TimeX Performance Benchmark and Feature Demonstration Main Class
 * 
 * This class provides a comprehensive performance testing suite that can run independently
 * to verify TimeX library performance and functionality completeness across different platforms.
 * 
 * Usage:
 * java -jar javaext-time-1.2.0.jar
 * 
 * Or:
 * java -cp javaext-time-1.2.0.jar com.xsyphon.javaext.TimeXBenchmark
 */
public class TimeXBenchmark {

    /**
     * Main function - Run comprehensive performance testing and functionality verification
     */
    public static void main(String[] args) {
        System.out.println("=== TimeX Cross-Platform High-Performance Timestamp Library ===");
        System.out.println("Version: 1.2.0");
        System.out.println("Project URL: https://github.com/pinealctx/javaext");
        System.out.println();
        
        // Display platform information
        displayPlatformInfo();
        
        // Library loading status check
        checkLibraryLoadStatus();
        
        // Run performance benchmarks
        runPerformanceBenchmarks();
        
        // Run precision comparison tests
        runPrecisionComparison();
        
        // Feature demonstration
        runFeatureDemonstration();
        
        System.out.println("\n=== Benchmark Complete ===");
    }

    /**
     * Display system and platform information
     */
    private static void displayPlatformInfo() {
        System.out.println("=== Platform Information ===");
        String osName = System.getProperty("os.name");
        String osArch = System.getProperty("os.arch");
        String javaVersion = System.getProperty("java.version");
        String javaVendor = System.getProperty("java.vendor");
        
        System.out.println("Operating System: " + osName);
        System.out.println("Architecture: " + osArch);
        System.out.println("Java Version: " + javaVersion);
        System.out.println("Java Vendor: " + javaVendor);
        
        // Determine expected native library name based on platform
        String platform = osName.toLowerCase().contains("mac") ? "macos" : 
                         osName.toLowerCase().contains("linux") ? "linux" : "unknown";
        String arch = osArch.toLowerCase().contains("aarch64") || osArch.toLowerCase().contains("arm") ? "arm64" : "amd64";
        String libExt = platform.equals("macos") ? "dylib" : "so";
        String expectedLib = String.format("/native/%s/%s/libjavaext_time.%s", platform, arch, libExt);
        
        System.out.println("Expected Native Library: " + expectedLib);
        System.out.println();
    }

    /**
     * Check native library loading status
     */
    private static void checkLibraryLoadStatus() {
        System.out.println("=== Native Library Status ===");
        
        // Check JNI library
        long jniTime = TimeX.unixNanoJNI();
        if (jniTime != -1) {
            System.out.println("✅ JNI Library: Successfully loaded and functional");
            System.out.println("   Sample timestamp: " + jniTime);
        } else {
            System.out.println("❌ JNI Library: Failed to load or not available");
        }
        
        // Check JNA library
        long jnaTime = TimeX.unixNanoJNA();
        if (jnaTime != -1) {
            System.out.println("✅ JNA Library: Successfully loaded and functional");
            System.out.println("   Sample timestamp: " + jnaTime);
        } else {
            System.out.println("❌ JNA Library: Failed to load or not available");
        }
        
        System.out.println();
    }

    /**
     * Run performance benchmarks for all timestamp methods
     */
    private static void runPerformanceBenchmarks() {
        System.out.println("=== Performance Benchmark Tests ===");
        final int iterations = 100_000; // 100K iterations for quick testing
        
        System.out.println("Warming up JVM...");
        // JVM warmup
        for (int i = 0; i < 50_000; i++) {
            System.nanoTime();
            TimeX.unixNanoInstant();
            TimeX.unixNanoHybrid();
            TimeX.unixNanoMilliPrecision();
            TimeX.unixNanoOptimized();
            TimeX.unixNanoJNI();
            TimeX.unixNanoJNA();
        }
        System.out.println("Warmup complete\n");

        System.out.printf("%-20s %10s %12s %15s\n", "Method", "Total(ms)", "Avg(ns)", "Throughput(ops/s)");
        System.out.println("─".repeat(70));

        // Test each method
        testMethod("System.nanoTime()", iterations, System::nanoTime);
        testMethod("Instant.now()", iterations, TimeX::unixNanoInstant);
        testMethod("System.hybrid", iterations, TimeX::unixNanoHybrid);
        testMethod("Millis precision", iterations, TimeX::unixNanoMilliPrecision);
        testMethod("Optimized", iterations, TimeX::unixNanoOptimized);
        
        // Test native methods
        testMethod("JNI Native", iterations, TimeX::unixNanoJNI);
        
        testMethod("JNA Native", iterations, TimeX::unixNanoJNA);
        
        System.out.println();
    }

    /**
     * Test method performance
     */
    private static void testMethod(String methodName, int iterations, TimeSupplier supplier) {
        System.gc(); // Suggest garbage collection
        
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            supplier.get();
        }
        long endTime = System.nanoTime();
        
        long totalTime = endTime - startTime;
        double avgTime = (double) totalTime / iterations;
        double throughput = 1_000_000_000.0 / avgTime;
        
        System.out.printf("%-20s %10.2f %12.2f %15.0f\n", 
                methodName, totalTime / 1_000_000.0, avgTime, throughput);
    }

    /**
     * Run precision comparison tests
     */
    private static void runPrecisionComparison() {
        System.out.println("=== Precision Comparison Test ===");
        
        // Take multiple samples for comparison
        long instantVsHybrid = TimeX.unixNanoInstant();
        long hybrid = TimeX.unixNanoHybrid();
        long instantVsMilli = TimeX.unixNanoInstant();
        long millis = TimeX.unixNanoMilliPrecision();
        long instantVsOptimized = TimeX.unixNanoInstant();
        long optimized = TimeX.unixNanoOptimized();
        
        long instantVsJNI = TimeX.unixNanoInstant();
        long jni = TimeX.unixNanoJNI();
        long instantVsJNA = TimeX.unixNanoInstant();
        long jna = TimeX.unixNanoJNA();
        
        // Check if native methods returned -1 (error)
        if (jni == -1) {
            System.out.println("JNI method not available");
            jni = instantVsJNI; // Use fallback value for comparison
        }
        
        if (jna == -1) {
            System.out.println("JNA method not available");
            jna = instantVsJNA; // Use fallback value for comparison
        }

        System.out.printf("%-18s: %d\n", "Instant method", instantVsHybrid);
        System.out.printf("%-18s: %d (diff: %+d ns)\n", "Hybrid method", hybrid, hybrid - instantVsHybrid);
        System.out.printf("%-18s: %d (diff: %+d ns)\n", "Millis precision", millis, millis - instantVsMilli);
        System.out.printf("%-18s: %d (diff: %+d ns)\n", "Optimized", optimized, optimized - instantVsOptimized);
        System.out.printf("%-18s: %d (diff: %+d ns)\n", "JNI native", jni, jni - instantVsJNI);
        System.out.printf("%-18s: %d (diff: %+d ns)\n", "JNA native", jna, jna - instantVsJNA);
        
        System.out.println();
    }

    /**
     * Demonstrate library features
     */
    private static void runFeatureDemonstration() {
        System.out.println("=== Feature Demonstration ===");
        
        // Time interval measurement using the main unixNano method
        System.out.println("Time interval measurement test:");
        long start = TimeX.unixNanoOptimized(); // Use optimized as default
        
        try {
            Thread.sleep(10); // Sleep 10ms
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long end = TimeX.unixNanoOptimized();
        long elapsed = end - start;
        
        System.out.printf("Measured interval: %d ns (%.2f ms)\n", elapsed, elapsed / 1_000_000.0);
        System.out.printf("Unix timestamps: start=%d, end=%d\n", start, end);
        
        // High frequency sampling test
        System.out.println("\nHigh frequency sampling test (1000 samples):");
        long[] samples = new long[1000];
        long samplingStart = System.nanoTime();
        
        for (int i = 0; i < samples.length; i++) {
            samples[i] = TimeX.unixNanoOptimized();
        }
        
        long samplingEnd = System.nanoTime();
        long samplingDuration = samplingEnd - samplingStart;
        
        // Calculate statistics
        long minInterval = Long.MAX_VALUE;
        long maxInterval = 0;
        long totalInterval = 0;
        int validIntervals = 0;
        
        for (int i = 1; i < samples.length; i++) {
            long interval = samples[i] - samples[i-1];
            if (interval > 0) { // Only count positive intervals
                minInterval = Math.min(minInterval, interval);
                maxInterval = Math.max(maxInterval, interval);
                totalInterval += interval;
                validIntervals++;
            }
        }
        
        if (validIntervals > 0) {
            double avgInterval = (double) totalInterval / validIntervals;
            double samplingRate = 1_000_000_000.0 / avgInterval;
            
            System.out.printf("Sampling duration: %.2f ms\n", samplingDuration / 1_000_000.0);
            System.out.printf("Average sampling rate: %.0f samples/sec\n", samplingRate);
            System.out.printf("Interval stats: min=%d ns, max=%d ns, avg=%.1f ns\n", 
                    minInterval, maxInterval, avgInterval);
        } else {
            System.out.println("Warning: No valid intervals detected (clock resolution too low)");
        }
        
        System.out.println();
    }

    /**
     * Functional interface for performance testing
     */
    @FunctionalInterface
    private interface TimeSupplier {
        long get();
    }
}
