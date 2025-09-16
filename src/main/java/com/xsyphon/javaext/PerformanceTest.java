package com.xsyphon.javaext;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Performance testing application for TimeX methods.
 * Tests each timestamp method with 1 million iterations
 * and reports average execution time in nanoseconds.
 * 
 * @author pinealctx
 * @version 1.2.0
 */
public class PerformanceTest {
    
    private static final int ITERATIONS = 1_000_000;
    private static final int WARMUP_ITERATIONS = 100_000;
    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#,###.##");
    
    /**
     * Performance test case definition.
     */
    private static class TestCase {
        final String name;
        final String description;
        final Supplier<Long> method;
        final boolean requiresNative;
        
        TestCase(String name, String description, Supplier<Long> method, boolean requiresNative) {
            this.name = name;
            this.description = description;
            this.method = method;
            this.requiresNative = requiresNative;
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=".repeat(80));
        System.out.println("TimeX Performance Test");
        System.out.println("=".repeat(80));
        System.out.println();
        
        // Display system information
        printSystemInfo();
        System.out.println();
        
        // Define test cases
        List<TestCase> testCases = createTestCases();
        
        // Run warmup
        System.out.println("Warming up JVM...");
        warmupJVM(testCases);
        System.out.println("Warmup completed.\n");
        
        // Run performance tests
        System.out.println("Starting performance tests (" + NUMBER_FORMAT.format(ITERATIONS) + " iterations each):");
        System.out.println("-".repeat(80));
        System.out.printf("%-25s %-15s %-15s %s%n", "Method", "Avg Time (ns)", "Total Time (ms)", "Status");
        System.out.println("-".repeat(80));
        
        List<TestResult> results = new ArrayList<>();
        
        for (TestCase testCase : testCases) {
            TestResult result = runPerformanceTest(testCase);
            results.add(result);
            
            if (result.successful) {
                System.out.printf("%-25s %-15s %-15s %s%n",
                    testCase.name,
                    NUMBER_FORMAT.format(result.avgTimeNanos),
                    NUMBER_FORMAT.format(result.totalTimeMillis),
                    "✓ OK"
                );
            } else {
                System.out.printf("%-25s %-15s %-15s %s%n",
                    testCase.name,
                    "N/A",
                    "N/A",
                    "✗ " + result.errorMessage
                );
            }
        }
        
        System.out.println("-".repeat(80));
        System.out.println();
        
        // Display summary
        printSummary(results);
        
        // Display comparative analysis
        printComparativeAnalysis(results);
    }
    
    /**
     * Create list of test cases to run.
     */
    private static List<TestCase> createTestCases() {
        List<TestCase> testCases = new ArrayList<>();
        
        testCases.add(new TestCase(
            "Instant API",
            "Standard Java Instant.now()",
            TimeX::unixNanoInstant,
            false
        ));
        
        testCases.add(new TestCase(
            "System Hybrid",
            "System.nanoTime() + offset",
            TimeX::unixNanoHybrid,
            false
        ));
        
        testCases.add(new TestCase(
            "Millis Precision",
            "System.currentTimeMillis() * 1M",
            TimeX::unixNanoMilliPrecision,
            false
        ));
        
        testCases.add(new TestCase(
            "Optimized Provider",
            "Optimized time provider algorithm",
            TimeX::unixNanoOptimized,
            false
        ));
        
        testCases.add(new TestCase(
            "JNI Native",
            "JNI clock_gettime()",
            TimeX::unixNanoJNI,
            true
        ));
        
        testCases.add(new TestCase(
            "JNA Native",
            "JNA clock_gettime()",
            TimeX::unixNanoJNA,
            true
        ));
        
        return testCases;
    }
    
    /**
     * Print system and platform information.
     */
    private static void printSystemInfo() {
        System.out.println("System Information:");
        System.out.println("  Java Version: " + System.getProperty("java.version"));
        System.out.println("  Java Vendor: " + System.getProperty("java.vendor"));
        System.out.println("  JVM Name: " + System.getProperty("java.vm.name"));
        System.out.println("  JVM Version: " + System.getProperty("java.vm.version"));
        System.out.println("  Platform: " + TimeX.getPlatformInfo());
        System.out.println("  Available Processors: " + Runtime.getRuntime().availableProcessors());
        System.out.println("  Max Memory: " + NUMBER_FORMAT.format(Runtime.getRuntime().maxMemory() / 1024 / 1024) + " MB");
        
        // Check native library availability
        System.out.println();
        System.out.println("Native Library Status:");
        System.out.println("  JNI Available: " + (TimeX.isJNIAvailable() ? "✓ YES" : "✗ NO"));
        if (!TimeX.isJNIAvailable()) {
            String error = NativeTimeJNI.getLoadError();
            if (error != null) {
                System.out.println("    Error: " + error);
            }
        }
        
        System.out.println("  JNA Available: " + (TimeX.isJNAAvailable() ? "✓ YES" : "✗ NO"));
        if (!TimeX.isJNAAvailable()) {
            String error = NativeTimeJNA.getLoadError();
            if (error != null) {
                System.out.println("    Error: " + error);
            }
        }
        
        if (TimeX.isJNAAvailable()) {
            System.out.println("  JNA Clock Info: " + NativeTimeJNA.getClockInfo());
        }
    }
    
    /**
     * Warm up the JVM to ensure stable performance measurements.
     */
    private static void warmupJVM(List<TestCase> testCases) {
        for (TestCase testCase : testCases) {
            if (testCase.requiresNative && !isNativeAvailable(testCase)) {
                continue; // Skip native methods if not available
            }
            
            try {
                for (int i = 0; i < WARMUP_ITERATIONS; i++) {
                    testCase.method.get();
                }
            } catch (Exception e) {
                // Ignore warmup errors
            }
        }
        
        // Force garbage collection
        System.gc();
        
        // Small delay to let GC complete
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Check if native libraries are available for the test case.
     */
    private static boolean isNativeAvailable(TestCase testCase) {
        if (!testCase.requiresNative) {
            return true;
        }
        
        if (testCase.name.contains("JNI")) {
            return TimeX.isJNIAvailable();
        } else if (testCase.name.contains("JNA")) {
            return TimeX.isJNAAvailable();
        }
        
        return true;
    }
    
    /**
     * Run performance test for a single method.
     */
    private static TestResult runPerformanceTest(TestCase testCase) {
        if (testCase.requiresNative && !isNativeAvailable(testCase)) {
            return new TestResult(false, 0, 0, "Native library not available");
        }
        
        try {
            // Validate the method works
            long testValue = testCase.method.get();
            if (testValue <= 0) {
                return new TestResult(false, 0, 0, "Invalid timestamp returned: " + testValue);
            }
            
            // Run performance test
            long startTime = System.nanoTime();
            
            for (int i = 0; i < ITERATIONS; i++) {
                testCase.method.get();
            }
            
            long endTime = System.nanoTime();
            long totalNanos = endTime - startTime;
            double avgNanos = (double) totalNanos / ITERATIONS;
            double totalMillis = totalNanos / 1_000_000.0;
            
            return new TestResult(true, avgNanos, totalMillis, null);
            
        } catch (Exception e) {
            return new TestResult(false, 0, 0, e.getMessage());
        }
    }
    
    /**
     * Test result holder.
     */
    private static class TestResult {
        final boolean successful;
        final double avgTimeNanos;
        final double totalTimeMillis;
        final String errorMessage;
        
        TestResult(boolean successful, double avgTimeNanos, double totalTimeMillis, String errorMessage) {
            this.successful = successful;
            this.avgTimeNanos = avgTimeNanos;
            this.totalTimeMillis = totalTimeMillis;
            this.errorMessage = errorMessage;
        }
    }
    
    /**
     * Print test summary.
     */
    private static void printSummary(List<TestResult> results) {
        System.out.println("Test Summary:");
        
        long successfulTests = results.stream().mapToLong(r -> r.successful ? 1 : 0).sum();
        System.out.println("  Total Tests: " + results.size());
        System.out.println("  Successful: " + successfulTests);
        System.out.println("  Failed: " + (results.size() - successfulTests));
        
        if (successfulTests > 0) {
            double minTime = results.stream()
                .filter(r -> r.successful)
                .mapToDouble(r -> r.avgTimeNanos)
                .min()
                .orElse(0.0);
            
            double maxTime = results.stream()
                .filter(r -> r.successful)
                .mapToDouble(r -> r.avgTimeNanos)
                .max()
                .orElse(0.0);
            
            System.out.println("  Fastest Method: " + NUMBER_FORMAT.format(minTime) + " ns");
            System.out.println("  Slowest Method: " + NUMBER_FORMAT.format(maxTime) + " ns");
            
            if (minTime > 0) {
                double speedup = maxTime / minTime;
                System.out.println("  Performance Range: " + NUMBER_FORMAT.format(speedup) + "x");
            }
        }
    }
    
    /**
     * Print comparative analysis of results.
     */
    private static void printComparativeAnalysis(List<TestResult> results) {
        List<TestResult> successfulResults = results.stream()
            .filter(r -> r.successful)
            .toList();
        
        if (successfulResults.isEmpty()) {
            System.out.println("\nNo successful tests for comparative analysis.");
            return;
        }
        
        System.out.println("\nComparative Analysis:");
        
        // Find the fastest method
        double fastestTime = successfulResults.stream()
            .mapToDouble(r -> r.avgTimeNanos)
            .min()
            .orElse(1.0);
        
        System.out.println("  Performance relative to fastest method:");
        
        List<TestCase> testCases = createTestCases();
        for (int i = 0; i < testCases.size() && i < results.size(); i++) {
            TestResult result = results.get(i);
            TestCase testCase = testCases.get(i);
            
            if (result.successful) {
                double relative = result.avgTimeNanos / fastestTime;
                System.out.printf("    %-25s: %6.2fx%n", testCase.name, relative);
            }
        }
    }
}
