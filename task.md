### Unix Nano Time Stamp

#### 在当前目录建立一个java项目，并实现一个TimeX类，在`TimeX.java`文件中，帮我实现几种不同获取系统Unix纳秒时间戳的方法，类似于golang中的`time.Now().UnixNano()`，并且写一个主函数测试这些方法的性能，要求每种方法都执行一百万次，并打印出每种方法的平均执行时间（纳秒）。请确保代码简洁高效，并且包含必要的注释说明。

- 标准 Java API - Instant，类似下面代码。
  ```java
    Instant now = Instant.now();
    return now.getEpochSecond() * 1_000_000_000L + now.getNano();
  ```

- System 时间方法混合
    ```java
        // 需要在应用启动时初始化一次基准点
        private static final long SYSTEM_NANOTIME_OFFSET;
        static {
            long currentTimeMillis = System.currentTimeMillis();
            long nanoTime = System.nanoTime();
            SYSTEM_NANOTIME_OFFSET = currentTimeMillis * 1_000_000L - nanoTime;
        }

        ...

        // 获取当前Unix纳秒时间戳
          ...
          return System.nanoTime() + SYSTEM_NANOTIME_OFFSET;
    ```

- JNI 直接调用系统 clock_gettime，支持 Linux （Amd64（x64） 和 ARM64）和 macOS（Amd64Amd64（x64） 和 ARM64）。
  打包成jar后，需要在jar中包含对应的so/dylib文件，便于打包后在不同平台上可以直接运用和引用。

- JNA 调用，类似于JNI，不过用JNA来做。

- Windows或别的平台不支持JNI和JNA，用nstant.now()方法来替代。

- 优化的 System.currentTimeMillis() (仅毫秒精度)，即System.currentTimeMillis() * 1_000_000L;

- 尝试一下下面的方法
```
    INSTANCE;

    private static final int NANOS_PER_MILLI = 1000000;
    public static TimeProvider CLOCK = INSTANCE;
    private long delta = 0L;

    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    public long currentTimeMicros() {
        return this.currentTimeNanos() / 1000L;
    }

    public long currentTimeNanos() {
        long nowNS = System.nanoTime();
        long nowMS = this.currentTimeMillis() * 1000000L;
        long estimate = nowNS + this.delta;
        if (estimate < nowMS) {
            this.delta = nowMS - nowNS;
            return nowMS;
        } else if (estimate > nowMS + 1000000L) {
            nowMS += 1000000L;
            this.delta = nowMS - nowNS;
            return nowMS;
        } else {
            return estimate;
        }
    }

    static {
        long start = System.currentTimeMillis();

        while(System.currentTimeMillis() < start + 5L) {
            INSTANCE.currentTimeNanos();
            Jvm.nanoPause();
        }

    }
```

- JNI和JNA可能需要在不同平台上编译，我们这个项目是github项目，可以在github action中做编译和打包。

- 我希望包名叫com.xsyphon.javaext

- 希望你为我做相应的maven工程或别的构建工程，能打包并发布的那种。

- 整个构建我希望可以在github上完成跨平台的构建。

- 可以用OpenJDK 64-Bit Server VM Corretto-21.0.5.11.1 (build 21.0.5+11-LTS, mixed mode, sharing)
    openjdk version "21.0.5" 2024-10-15 LTS
    这个jdk版本来构建。
