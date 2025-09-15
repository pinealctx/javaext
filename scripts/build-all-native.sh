#!/bin/bash

# Script to build native libraries for all supported platforms
# This script simulates what GitHub Actions will do

set -e

echo "=== Building Native Libraries for All Platforms ==="

# Get script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

cd "$PROJECT_DIR"

# Detect current platform
CURRENT_OS=$(uname -s | tr '[:upper:]' '[:lower:]')
CURRENT_ARCH=$(uname -m)

# Normalize architecture names
case $CURRENT_ARCH in
    x86_64)
        CURRENT_ARCH="amd64"
        ;;
    aarch64|arm64)
        CURRENT_ARCH="arm64"
        ;;
    *)
        echo "Unsupported architecture: $CURRENT_ARCH"
        exit 1
        ;;
esac

echo "Current platform: $CURRENT_OS-$CURRENT_ARCH"

# Check prerequisites
if ! command -v gcc &> /dev/null && ! command -v clang &> /dev/null; then
    echo "Error: No C compiler found (gcc or clang required)"
    exit 1
fi

if [ -z "$JAVA_HOME" ]; then
    if command -v javac &> /dev/null; then
        export JAVA_HOME="$(dirname "$(dirname "$(which javac)")")"
        echo "Auto-detected JAVA_HOME: $JAVA_HOME"
    else
        echo "Error: JAVA_HOME not set and javac not found"
        exit 1
    fi
fi

# Function to build for a specific platform
build_for_platform() {
    local platform=$1
    local arch=$2
    local compiler=$3
    local extra_flags=$4
    
    echo "Building for $platform-$arch using $compiler..."
    
    # Create target directory
    local target_dir="target/native/$platform/$arch"
    mkdir -p "$target_dir"
    
    # Determine library extension and shared flag
    local lib_ext=""
    local shared_flag=""
    local lib_name=""
    local include_path=""
    
    case $platform in
        linux)
            lib_ext="so"
            shared_flag="-shared"
            lib_name="libjavaext_time.so"
            include_path="$JAVA_HOME/include/linux"
            extra_flags="$extra_flags -lrt"
            ;;
        macos|darwin)
            lib_ext="dylib"
            shared_flag="-dynamiclib"
            lib_name="libjavaext_time.dylib"
            include_path="$JAVA_HOME/include/darwin"
            ;;
        *)
            echo "Unsupported platform: $platform"
            return 1
            ;;
    esac
    
    # Build command
    local build_cmd="$compiler -O3 -Wall -Wextra -fPIC -std=c99 \
        -I\"$JAVA_HOME/include\" \
        -I\"$include_path\" \
        $shared_flag \
        -o $target_dir/$lib_name \
        src/main/native/javaext_time.c \
        $extra_flags"
    
    echo "Executing: $build_cmd"
    eval $build_cmd
    
    if [ $? -eq 0 ]; then
        echo "✓ Successfully built $platform-$arch"
        ls -la "$target_dir/$lib_name"
        file "$target_dir/$lib_name"
        
        # Copy to resources directory
        local resource_dir="src/main/resources/native/$platform/$arch"
        mkdir -p "$resource_dir"
        cp "$target_dir/$lib_name" "$resource_dir/"
        echo "✓ Copied to $resource_dir/"
    else
        echo "✗ Failed to build $platform-$arch"
        return 1
    fi
}

# Build for current platform
echo ""
echo "=== Building for current platform: $CURRENT_OS-$CURRENT_ARCH ==="

if [ "$CURRENT_OS" = "linux" ]; then
    build_for_platform "linux" "$CURRENT_ARCH" "gcc" ""
    
    # Try cross-compilation for ARM64 if on x64 Linux
    if [ "$CURRENT_ARCH" = "amd64" ] && command -v aarch64-linux-gnu-gcc &> /dev/null; then
        echo ""
        echo "=== Cross-compiling for linux-arm64 ==="
        build_for_platform "linux" "arm64" "aarch64-linux-gnu-gcc" ""
    fi
    
elif [ "$CURRENT_OS" = "darwin" ]; then
    build_for_platform "macos" "$CURRENT_ARCH" "clang" ""
    
    # Try cross-compilation for other architecture on macOS
    if [ "$CURRENT_ARCH" = "amd64" ]; then
        echo ""
        echo "=== Cross-compiling for macos-arm64 ==="
        build_for_platform "macos" "arm64" "clang" "-target arm64-apple-macos10.12"
    elif [ "$CURRENT_ARCH" = "arm64" ]; then
        echo ""
        echo "=== Cross-compiling for macos-amd64 ==="
        build_for_platform "macos" "amd64" "clang" "-target x86_64-apple-macos10.12"
    fi
fi

echo ""
echo "=== Build Summary ==="
echo "Built libraries:"
find target/native -name "*.so" -o -name "*.dylib" | sort

echo ""
echo "Libraries in resources:"
find src/main/resources/native -name "*.so" -o -name "*.dylib" | sort

echo ""
echo "=== Native library build completed! ==="

# Test the build
echo ""
echo "=== Testing the build ==="
if mvn test -Pskip-native -q; then
    echo "✓ Java tests passed"
else
    echo "✗ Java tests failed"
    exit 1
fi

echo ""
echo "=== Running performance test ==="
mvn exec:java -Dexec.mainClass="com.xsyphon.javaext.PerformanceTest" -Pskip-native -q
