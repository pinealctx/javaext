# Java Extensions for High-Performance Time Operations

[![CI](https://github.com/pinealctx/javaext/actions/workflows/basic-ci.yml/badge.svg)](https://github.com/pinealctx/javaext/actions/workflows/basic-ci.yml)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.xsyphon/javaext/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.xsyphon/javaext)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

A high-performance Java library for obtaining Unix nanosecond timestamps with multiple implementation strategies, similar to Go's `time.Now().UnixNano()`.

## Features

- **Multiple Implementation Strategies**: Choose the best approach for your use case
- **Cross-Platform Support**: Works on Linux, macOS, and Windows
- **Native Performance**: JNI and JNA implementations for maximum speed
- **Graceful Fallbacks**: Automatically falls back to pure Java when native libraries aren't available
- **Comprehensive Testing**: Extensive unit tests and performance benchmarks
- **Zero External Dependencies**: Core functionality requires only standard JDK

## Quick Start

### Maven

```xml
<dependency>
    <groupId>com.xsyphon</groupId>
    <artifactId>javaext</artifactId>
    <version>1.1.0</version>
</dependency>
```

### Gradle

```gradle
implementation 'com.xsyphon:javaext:1.1.0'
```

### Basic Usage

```java
import com.xsyphon.javaext.TimeX;

public class Example {
    public static void main(String[] args) {
        // Get Unix nanosecond timestamp (similar to Go's time.Now().UnixNano())
        long timestamp = TimeX.unixNanoInstant();
        System.out.println("Unix nanoseconds: " + timestamp);
        
        // Use the fastest available method
        long fastTimestamp = TimeX.unixNanoOptimized();
        System.out.println("Optimized timestamp: " + fastTimestamp);
        
        // Check platform capabilities
        System.out.println("JNI available: " + TimeX.isJNIAvailable());
        System.out.println("JNA available: " + TimeX.isJNAAvailable());
    }
}
```

## Available Methods

The `TimeX` class provides several methods for obtaining Unix nanosecond timestamps, each with different performance characteristics:

### 1. Standard Java API (`unixNanoInstant()`)
```java
long timestamp = TimeX.unixNanoInstant();
```
- Uses `Instant.now()` for maximum compatibility
- Good accuracy, moderate performance
- Works on all platforms

### 2. System Hybrid (`unixNanoHybrid()`)
```java
long timestamp = TimeX.unixNanoHybrid();
```
- Combines `System.currentTimeMillis()` with `System.nanoTime()`
- Better performance than Instant API
- Calibrated at startup for accuracy

### 3. JNI Native (`unixNanoJNI()`)
```java
long timestamp = TimeX.unixNanoJNI();
```
- Direct native `clock_gettime()` call via JNI
- Maximum performance on supported platforms
- Falls back to Instant API if native library unavailable

### 4. JNA Native (`unixNanoJNA()`)
```java
long timestamp = TimeX.unixNanoJNA();
```
- Native `clock_gettime()` call via JNA
- Good performance without JNI complexity
- Falls back to Instant API if JNA unavailable

### 5. Millisecond Precision (`unixNanoMilliPrecision()`)
```java
long timestamp = TimeX.unixNanoMilliPrecision();
```
- Fastest method with millisecond precision
- Based on `System.currentTimeMillis() * 1,000,000`
- Use when nanosecond precision isn't required

### 6. Optimized Provider (`unixNanoOptimized()`)
```java
long timestamp = TimeX.unixNanoOptimized();
```
- Adaptive algorithm balancing speed and precision
- Self-calibrating for optimal performance
- Recommended for most use cases

## Performance Benchmarks

Run the built-in performance test to see how methods perform on your system:

```bash
# With Maven
mvn exec:java -Dexec.mainClass="com.xsyphon.javaext.PerformanceTest"

# With Gradle
./gradlew performanceTest
```

Example output:
```
=================================================================
TimeX Performance Test
=================================================================

System Information:
  Java Version: 21.0.5
  Platform: OS: mac os x, Architecture: aarch64
  JNI Available: ✓ YES
  JNA Available: ✓ YES

Starting performance tests (1,000,000 iterations each):
-------------------------------------------------------------------
Method                   Avg Time (ns)   Total Time (ms)  Status
-------------------------------------------------------------------
Instant API              45.23           45.23            ✓ OK
System Hybrid            12.34           12.34            ✓ OK
Millis Precision         8.91            8.91             ✓ OK
Optimized Provider       15.67           15.67            ✓ OK
JNI Native               6.78            6.78             ✓ OK
JNA Native               23.45           23.45            ✓ OK
-------------------------------------------------------------------
```

## Platform Support

### Supported Platforms
- **Linux**: x86_64 (AMD64) and ARM64 architectures
- **macOS**: x86_64 (Intel) and ARM64 (Apple Silicon) architectures
- **Windows**: All architectures (pure Java fallback)

### Native Library Support
- Native libraries are automatically extracted and loaded from the JAR
- JNI libraries: `libjavaext_time.so` (Linux), `libjavaext_time.dylib` (macOS)
- JNA support: Uses system `libc` for `clock_gettime()`

## Building from Source

### Prerequisites
- JDK 21 or higher (Corretto recommended)
- Maven 3.6+ or Gradle 8.0+
- GCC compiler (for native libraries)
- Make (for native libraries)

### Build with Maven
```bash
# Build everything including native libraries
mvn clean package

# Build without native libraries (pure Java only)
mvn clean package -Pskip-native

# Run tests
mvn test

# Run performance tests
mvn exec:java -Dexec.mainClass="com.xsyphon.javaext.PerformanceTest"
```

### Build with Gradle
```bash
# Build everything including native libraries
./gradlew build

# Build without native libraries
./gradlew build -PskipNativeBuild=true

# Run tests
./gradlew test

# Run performance tests
./gradlew performanceTest
```

### Build Native Libraries Only
```bash
# Build native libraries for current platform
make all

# Install libraries to resources directory
make install

# Clean build artifacts
make clean
```

## Cross-Platform Build with GitHub Actions

The project includes comprehensive GitHub Actions workflows for automated cross-platform builds:

### Workflows Overview

1. **Basic CI Tests** (`.github/workflows/basic-ci.yml`)
   - Fast compilation and testing on Ubuntu and macOS
   - Runs on every push and pull request
   - Skips native library compilation for speed

2. **Native Library Build** (`.github/workflows/build-native.yml`)
   - Builds native libraries for all supported platforms:
     - **Linux x64** (gcc on ubuntu-latest)
     - **Linux ARM64** (cross-compilation with gcc-aarch64-linux-gnu)
     - **macOS x64** (clang on macos-13)
     - **macOS ARM64** (clang on macos-latest)
   - Creates release artifacts with all native libraries included
   - Runs comprehensive tests with native libraries

### Supported Platforms and Architectures

| Platform | Architecture | Compiler | Runner | Status |
|----------|-------------|----------|---------|---------|
| Linux | x86_64 (AMD64) | GCC | ubuntu-latest | ✅ Native |
| Linux | ARM64 (AArch64) | GCC cross-compile | ubuntu-latest | ✅ Cross-compile |
| macOS | x86_64 (Intel) | Clang | macos-13 | ✅ Native |
| macOS | ARM64 (Apple Silicon) | Clang | macos-latest | ✅ Native |
| Windows | x86_64 | - | - | ⚠️ Pure Java fallback |

### GitHub Actions Build Process

1. **Checkout and Setup**: Code checkout and JDK 21 setup
2. **Platform-specific compilation**: Each platform builds its native library
3. **Artifact collection**: Native libraries are uploaded as artifacts
4. **Integration testing**: Libraries are tested on their target platforms
5. **Release packaging**: All libraries are bundled into the final JAR

### Local Cross-Platform Building

For local development and testing:

```bash
# Build native libraries for current platform and cross-compile when possible
./scripts/build-all-native.sh

# Build only for current platform
make all && make install

# Manual cross-compilation examples:

# Linux ARM64 (from Linux x64 with cross-compiler)
sudo apt-get install gcc-aarch64-linux-gnu
aarch64-linux-gnu-gcc -O3 -Wall -Wextra -fPIC -std=c99 \
  -I"$JAVA_HOME/include" -I"$JAVA_HOME/include/linux" \
  -shared -o target/native/linux/arm64/libjavaext_time.so \
  src/main/native/javaext_time.c -lrt

# macOS cross-compilation (from macOS)
clang -O3 -Wall -Wextra -fPIC -std=c99 \
  -I"$JAVA_HOME/include" -I"$JAVA_HOME/include/darwin" \
  -target arm64-apple-macos10.12 -dynamiclib \
  -o target/native/macos/arm64/libjavaext_time.dylib \
  src/main/native/javaext_time.c
```

### Release Process

When you push a tag (e.g., `v1.0.0`):

1. All native libraries are built automatically
2. A release JAR is created containing all platform libraries
3. The JAR is uploaded as a GitHub release artifact
4. Maven Central publishing is triggered (if configured)

### Native Library Loading

The application automatically detects and loads the appropriate native library:

```java
// Library loading is automatic and transparent
long timestamp = TimeX.unixNanoJNI(); // Uses native library if available

// Check availability
if (TimeX.isJNIAvailable()) {
    System.out.println("JNI native library loaded successfully");
} else {
    System.out.println("Using pure Java fallback");
}
```

## API Documentation

### Core Class: `TimeX`

#### Static Methods

| Method | Description | Platform Support | Performance |
|--------|-------------|-------------------|-------------|
| `unixNanoInstant()` | Standard Java Instant API | All | Moderate |
| `unixNanoHybrid()` | System time hybrid | All | Good |
| `unixNanoJNI()` | JNI native implementation | Linux, macOS | Excellent |
| `unixNanoJNA()` | JNA native implementation | Linux, macOS | Good |
| `unixNanoMilliPrecision()` | Millisecond precision only | All | Excellent |
| `unixNanoOptimized()` | Adaptive optimized algorithm | All | Very Good |
| `getPlatformInfo()` | Platform information string | All | N/A |
| `isJNIAvailable()` | Check JNI library status | All | N/A |
| `isJNAAvailable()` | Check JNA library status | All | N/A |

#### Error Handling

All methods are designed to be robust:
- Native methods fall back to pure Java implementations on errors
- No exceptions thrown for normal operation
- Platform incompatibilities handled gracefully

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature-name`
3. Make your changes and add tests
4. Run the test suite: `mvn test` or `./gradlew test`
5. Run performance tests: `mvn exec:java -Dexec.mainClass="com.xsyphon.javaext.PerformanceTest"`
6. Submit a pull request

### Development Guidelines

- Maintain compatibility with JDK 21+
- Add unit tests for new functionality
- Include performance benchmarks for timing-related changes
- Update documentation for API changes
- Follow existing code style and conventions

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Inspired by Go's `time.Now().UnixNano()` functionality
- Performance optimization techniques adapted from high-frequency trading systems
- Cross-platform build strategies from the Java ecosystem best practices

---

**Note**: This library prioritizes performance and accuracy for time-sensitive applications. Choose the appropriate method based on your specific requirements for precision vs. performance.
