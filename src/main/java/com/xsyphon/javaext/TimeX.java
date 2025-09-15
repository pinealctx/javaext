import com.xsyphon.javaext.TimeXJna;
package com.xsyphon.javaext;

import java.time.Instant;

/**
 * TimeX 提供多种获取系统 Unix 纳秒时间戳的方法，并可进行性能测试。
 */
public class TimeX {
    // 方法1：标准 Java API - Instant
    public static long unixNanoByInstant() {
        Instant now = Instant.now();
        return now.getEpochSecond() * 1_000_000_000L + now.getNano();
    }

    // 方法2：System 时间方法混合
    private static final long SYSTEM_NANOTIME_OFFSET;
    static {
        long currentTimeMillis = System.currentTimeMillis();
        long nanoTime = System.nanoTime();
        SYSTEM_NANOTIME_OFFSET = currentTimeMillis * 1_000_000L - nanoTime;
    }
    public static long unixNanoBySystemMix() {
        return System.nanoTime() + SYSTEM_NANOTIME_OFFSET;
    }

    // 方法3：优化的 System.currentTimeMillis()（毫秒精度）
    public static long unixNanoByMillis() {
        return System.currentTimeMillis() * 1_000_000L;
    }

    // 方法4：INSTANCE 方案（带漂移校正）
    private static final int NANOS_PER_MILLI = 1_000_000;
    public static final TimeProvider CLOCK = new TimeProvider();
    public static class TimeProvider {
        private long delta = 0L;
        public long currentTimeMillis() {
            return System.currentTimeMillis();
        }
        public long currentTimeMicros() {
            return this.currentTimeNanos() / 1_000L;
        }
        public long currentTimeNanos() {
            long nowNS = System.nanoTime();
            long nowMS = this.currentTimeMillis() * 1_000_000L;
            long estimate = nowNS + this.delta;
            if (estimate < nowMS) {
                this.delta = nowMS - nowNS;
                return nowMS;
            } else if (estimate > nowMS + 1_000_000L) {
                nowMS += 1_000_000L;
                this.delta = nowMS - nowNS;
                return nowMS;
            } else {
                return estimate;
            }
        }
    }
    static {
        long start = System.currentTimeMillis();
        while(System.currentTimeMillis() < start + 5L) {
            CLOCK.currentTimeNanos();
            // Jvm.nanoPause(); // 这里可用 Thread.yield() 代替
            Thread.yield();
        }
    }

    // 方法5：JNA 方式
    public static long unixNanoByJna() {
        return TimeXJna.unixNanoByJna();
    }
    // 方法6：JNI 方式
    public static long unixNanoByJni() {
        return TimeXJni.unixNanoByJni();
    }
    // TODO: 方法7 Windows fallback，可后续补充

    /**
     * 性能测试主函数
     */
    public static void main(String[] args) {
        final int N = 1_000_000;
        System.out.println("性能测试，每种方法执行 " + N + " 次，单位: 纳秒");
        test("Instant", N, TimeX::unixNanoByInstant);
        test("SystemMix", N, TimeX::unixNanoBySystemMix);
        test("Millis", N, TimeX::unixNanoByMillis);
        test("INSTANCE", N, CLOCK::currentTimeNanos);
        try {
            test("JNA", N, TimeX::unixNanoByJna);
        } catch (Throwable e) {
            System.out.println("JNA 测试不可用: " + e);
        }
        try {
            test("JNI", N, TimeX::unixNanoByJni);
        } catch (Throwable e) {
            System.out.println("JNI 测试不可用: " + e);
        }
    }

    private static void test(String name, int n, TimeSupplier supplier) {
        long sum = 0;
        long t0 = System.nanoTime();
        for (int i = 0; i < n; i++) {
            sum += supplier.get();
        }
        long t1 = System.nanoTime();
        double avg = (t1 - t0) * 1.0 / n;
        System.out.printf("%-10s 平均耗时: %.2f ns, sum=%d\n", name, avg, sum);
    }

    @FunctionalInterface
    interface TimeSupplier {
        long get();
    }
}
