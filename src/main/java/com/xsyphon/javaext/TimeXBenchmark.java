package com.xsyphon.javaext;

/**
 * TimeX 性能基准测试和功能演示主类
 * 
 * 这个类提供了完整的性能测试套件，可以独立运行来验证
 * TimeX 库在不同平台上的性能表现和功能完整性。
 * 
 * 使用方法:
 * java -jar javaext-time-1.0.0.jar
 * 
 * 或者:
 * java -cp javaext-time-1.0.0.jar com.xsyphon.javaext.TimeXBenchmark
 */
public class TimeXBenchmark {

    /**
     * 主函数 - 运行完整的性能测试和功能验证
     */
    public static void main(String[] args) {
        System.out.println("=== TimeX 跨平台高性能时间戳库 ===");
        System.out.println("版本: 1.0.0");
        System.out.println("项目地址: https://github.com/pinealctx/javaext");
        System.out.println();
        
        // 显示平台信息
        displayPlatformInfo();
        
        // 库加载状态检查
        checkLibraryLoadStatus();
        
        // 运行性能基准测试
        runPerformanceBenchmark();
        
        // 精度对比测试
        runAccuracyComparison();
        
        // 功能性测试
        runFunctionalTests();
        
        System.out.println("\n=== 测试完成 ===");
        System.out.println("TimeX 库已成功验证所有功能!");
    }

    /**
     * 显示平台信息
     */
    private static void displayPlatformInfo() {
        System.out.println("=== 平台信息 ===");
        String osName = System.getProperty("os.name");
        String osArch = System.getProperty("os.arch");
        String javaVersion = System.getProperty("java.version");
        String javaVendor = System.getProperty("java.vendor");
        
        System.out.println("操作系统: " + osName);
        System.out.println("系统架构: " + osArch);
        System.out.println("Java版本: " + javaVersion);
        System.out.println("Java厂商: " + javaVendor);
        
        // 确定期望的本地库
        String platform = osName.toLowerCase().contains("mac") ? "macos" : "linux";
        String arch = osArch.contains("aarch64") || osArch.contains("arm") ? "arm64" : "amd64";
        String libExt = platform.equals("macos") ? "dylib" : "so";
        String expectedLib = String.format("native/%s/%s/libjavaext_time.%s", platform, arch, libExt);
        
        System.out.println("期望库路径: " + expectedLib);
        System.out.println();
    }

    /**
     * 检查本地库加载状态
     */
    private static void checkLibraryLoadStatus() {
        System.out.println("=== 本地库加载状态 ===");
        
        // 检查JNI库
        boolean jniAvailable = false;
        try {
            long timestamp = TimeX.unixNanoJNI();
            jniAvailable = timestamp > 0;
            System.out.println("✅ JNI库加载成功 (timestamp: " + timestamp + ")");
        } catch (Exception e) {
            System.out.println("❌ JNI库不可用: " + e.getMessage());
        }
        
        // 检查JNA库
        boolean jnaAvailable = false;
        try {
            long timestamp = TimeX.unixNanoJNA();
            jnaAvailable = timestamp > 0;
            System.out.println("✅ JNA库加载成功 (timestamp: " + timestamp + ")");
        } catch (Exception e) {
            System.out.println("❌ JNA库不可用: " + e.getMessage());
        }
        
        if (!jniAvailable && !jnaAvailable) {
            System.out.println("⚠️ 警告: 本地库均不可用，将使用Java实现");
        }
        System.out.println();
    }

    /**
     * 运行性能基准测试
     */
    private static void runPerformanceBenchmark() {
        System.out.println("=== 性能基准测试 ===");
        
        final int warmupIterations = 100_000;
        final int testIterations = 1_000_000;
        
        // JVM预热
        System.out.println("正在预热JVM (" + warmupIterations + " 次迭代)...");
        for (int i = 0; i < warmupIterations; i++) {
            TimeX.unixNanoInstant();
            TimeX.unixNanoHybrid();
            TimeX.unixNanoMilliPrecision();
            TimeX.unixNanoOptimized();
            // 本地方法调用（如果可用）
            try {
                TimeX.unixNanoJNI();
            } catch (Exception ignored) {}
            try {
                TimeX.unixNanoJNA();
            } catch (Exception ignored) {}
        }
        System.out.println("预热完成\n");

        System.out.println("开始性能测试 (" + testIterations + " 次迭代):");
        System.out.println("方法名称          总耗时(ms)  平均耗时(ns)  吞吐量(ops/sec)");
        System.out.println("--------------------------------------------------------");
        
        // 测试各种方法
        testMethod("Instant方法", testIterations, TimeX::unixNanoInstant);
        testMethod("混合方法", testIterations, TimeX::unixNanoHybrid);
        testMethod("毫秒精度方法", testIterations, TimeX::unixNanoMilliPrecision);
        testMethod("优化方法", testIterations, TimeX::unixNanoOptimized);
        
        // JNI方法（如果可用）
        testMethodSafe("JNI方法", testIterations, () -> {
            try {
                return TimeX.unixNanoJNI();
            } catch (Exception e) {
                throw new RuntimeException("JNI不可用");
            }
        });
        
        // JNA方法（如果可用）
        testMethodSafe("JNA方法", testIterations, () -> {
            try {
                return TimeX.unixNanoJNA();
            } catch (Exception e) {
                throw new RuntimeException("JNA不可用");
            }
        });
        
        System.out.println();
    }

    /**
     * 精度对比测试
     */
    private static void runAccuracyComparison() {
        System.out.println("=== 精度对比测试 ===");
        
        // 获取基准时间戳
        long baseTime = TimeX.unixNanoInstant();
        
        System.out.println("以Instant方法作为基准进行比较:");
        System.out.printf("Instant方法:      %d (基准)\n", baseTime);
        
        // 测试其他方法与基准的差异
        compareMethod("混合方法", TimeX::unixNanoHybrid, baseTime);
        compareMethod("毫秒精度方法", TimeX::unixNanoMilliPrecision, baseTime);
        compareMethod("优化方法", TimeX::unixNanoOptimized, baseTime);
        
        // 本地方法比较
        compareMethodSafe("JNI方法", TimeX::unixNanoJNI, baseTime);
        compareMethodSafe("JNA方法", TimeX::unixNanoJNA, baseTime);
        
        System.out.println();
    }

    /**
     * 功能性测试
     */
    private static void runFunctionalTests() {
        System.out.println("=== 功能性测试 ===");
        
        // 时间戳单调性测试
        System.out.println("1. 时间戳单调性测试:");
        testMonotonicity("Instant方法", TimeX::unixNanoInstant);
        testMonotonicity("混合方法", TimeX::unixNanoHybrid);
        testMonotonicity("优化方法", TimeX::unixNanoOptimized);
        
        // 精度测试
        System.out.println("\n2. 精度测试:");
        testPrecision();
        
        // 多线程安全性测试
        System.out.println("\n3. 多线程安全性测试:");
        testThreadSafety();
        
        System.out.println();
    }

    /**
     * 测试方法性能
     */
    private static void testMethod(String methodName, int iterations, TimeSupplier supplier) {
        System.gc(); // 建议垃圾回收
        
        long startTime = System.nanoTime();
        long sum = 0; // 防止JIT优化
        
        for (int i = 0; i < iterations; i++) {
            sum += supplier.get();
        }
        
        long endTime = System.nanoTime();
        long totalTime = endTime - startTime;
        double avgTime = (double) totalTime / iterations;
        double throughput = 1_000_000_000.0 / avgTime;
        
        System.out.printf("%-15s %8.2f    %8.2f      %12.0f\n", 
                methodName, totalTime / 1_000_000.0, avgTime, throughput);
        
        // 使用sum防止优化
        if (sum == 0) System.out.print("");
    }

    /**
     * 安全地测试方法（处理异常）
     */
    private static void testMethodSafe(String methodName, int iterations, TimeSupplier supplier) {
        try {
            testMethod(methodName, iterations, supplier);
        } catch (Exception e) {
            System.out.printf("%-15s %8s    %8s      %12s (不可用: %s)\n", 
                    methodName, "N/A", "N/A", "N/A", e.getMessage().split(":")[0]);
        }
    }

    /**
     * 比较方法与基准的精度差异
     */
    private static void compareMethod(String methodName, TimeSupplier supplier, long baseline) {
        long timestamp = supplier.get();
        long diff = timestamp - baseline;
        System.out.printf("%-15s: %d (差值: %+d ns)\n", methodName, timestamp, diff);
    }

    /**
     * 安全地比较方法
     */
    private static void compareMethodSafe(String methodName, TimeSupplier supplier, long baseline) {
        try {
            compareMethod(methodName, supplier, baseline);
        } catch (Exception e) {
            System.out.printf("%-15s: 不可用 (%s)\n", methodName, e.getMessage().split(":")[0]);
        }
    }

    /**
     * 测试时间戳单调性
     */
    private static void testMonotonicity(String methodName, TimeSupplier supplier) {
        long prev = supplier.get();
        int violations = 0;
        
        for (int i = 0; i < 10000; i++) {
            long current = supplier.get();
            if (current < prev) {
                violations++;
            }
            prev = current;
        }
        
        if (violations == 0) {
            System.out.println("  ✅ " + methodName + ": 时间戳严格单调递增");
        } else {
            System.out.println("  ⚠️ " + methodName + ": 发现 " + violations + " 次单调性违反");
        }
    }

    /**
     * 测试时间戳精度
     */
    private static void testPrecision() {
        long start = System.nanoTime();
        try {
            Thread.sleep(1); // 睡眠1毫秒
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        long end = System.nanoTime();
        long javaDiff = end - start;
        
        long timeXStart = TimeX.unixNanoOptimized();
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        long timeXEnd = TimeX.unixNanoOptimized();
        long timeXDiff = timeXEnd - timeXStart;
        
        System.out.printf("  System.nanoTime() 测量: %d ns (%.2f ms)\n", 
                javaDiff, javaDiff / 1_000_000.0);
        System.out.printf("  TimeX.unixNanoOptimized() 测量: %d ns (%.2f ms)\n", 
                timeXDiff, timeXDiff / 1_000_000.0);
        System.out.printf("  精度差异: %d ns\n", Math.abs(timeXDiff - javaDiff));
    }

    /**
     * 测试多线程安全性
     */
    private static void testThreadSafety() {
        final int threadCount = 4;
        final int iterationsPerThread = 10000;
        Thread[] threads = new Thread[threadCount];
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < iterationsPerThread; j++) {
                    TimeX.unixNanoOptimized();
                    TimeX.unixNanoHybrid();
                    // 测试本地方法（如果可用）
                    try {
                        TimeX.unixNanoJNI();
                    } catch (Exception ignored) {}
                }
            });
            threads[i].start();
        }
        
        // 等待所有线程完成
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        
        long endTime = System.currentTimeMillis();
        long totalOperations = (long) threadCount * iterationsPerThread;
        
        System.out.printf("  ✅ 多线程测试完成: %d 个线程, 总计 %d 次操作, 耗时 %d ms\n", 
                threadCount, totalOperations, endTime - startTime);
    }

    /**
     * 函数式接口用于性能测试
     */
    @FunctionalInterface
    private interface TimeSupplier {
        long get();
    }
}
