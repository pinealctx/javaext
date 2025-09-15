#!/bin/bash

# Build script for native libraries
# This script builds native libraries for the current platform

set -e

echo "Building native libraries..."

# Get script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

cd "$PROJECT_DIR"

# Check if we have the necessary tools
if ! command -v gcc &> /dev/null; then
    echo "Error: GCC compiler not found. Please install GCC."
    exit 1
fi

if ! command -v make &> /dev/null; then
    echo "Error: Make not found. Please install Make."
    exit 1
fi

# Check for JAVA_HOME
if [ -z "$JAVA_HOME" ]; then
    if command -v javac &> /dev/null; then
        export JAVA_HOME="$(dirname "$(dirname "$(which javac)")")"
        echo "Auto-detected JAVA_HOME: $JAVA_HOME"
    else
        echo "Error: JAVA_HOME not set and javac not found in PATH."
        echo "Please set JAVA_HOME environment variable."
        exit 1
    fi
fi

# Display platform information
echo "Platform: $(uname -s)-$(uname -m)"
echo "JAVA_HOME: $JAVA_HOME"

# Build using Makefile
echo "Building native library..."
make clean
make all

# Install to resources directory
echo "Installing library to resources..."
make install

echo "Native library build completed successfully!"

# List built libraries
echo "Built libraries:"
find target/native -name "*.so" -o -name "*.dylib" -o -name "*.dll" 2>/dev/null | sort

echo "Installed libraries:"
find src/main/resources/native -name "*.so" -o -name "*.dylib" -o -name "*.dll" 2>/dev/null | sort
