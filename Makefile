# Makefile for building native libraries across platforms
# Supports Linux and macOS with AMD64 and ARM64 architectures

# Detect platform
UNAME_S := $(shell uname -s)
UNAME_M := $(shell uname -m)

# Normalize architecture names
ifeq ($(UNAME_M),x86_64)
    ARCH := amd64
else ifeq ($(UNAME_M),aarch64)
    ARCH := arm64
else ifeq ($(UNAME_M),arm64)
    ARCH := arm64
else
    $(error Unsupported architecture: $(UNAME_M))
endif

# Platform-specific settings
ifeq ($(UNAME_S),Linux)
    PLATFORM := linux
    LIB_EXT := so
    LIB_PREFIX := lib
    SHARED_FLAG := -shared
    PIC_FLAG := -fPIC
else ifeq ($(UNAME_S),Darwin)
    PLATFORM := macos
    LIB_EXT := dylib
    LIB_PREFIX := lib
    SHARED_FLAG := -dynamiclib
    PIC_FLAG := -fPIC
else
    $(error Unsupported platform: $(UNAME_S))
endif

# Java home detection
ifndef JAVA_HOME
    JAVA_HOME := $(shell dirname $(shell dirname $(shell which javac)))
endif

# Compiler settings
CC := gcc
CFLAGS := -O3 -Wall -Wextra $(PIC_FLAG) -std=c99
INCLUDES := -I"$(JAVA_HOME)/include"

# Platform-specific includes and libraries
ifeq ($(PLATFORM),linux)
    INCLUDES += -I"$(JAVA_HOME)/include/linux"
    LIBS := -lrt
else ifeq ($(PLATFORM),macos)
    INCLUDES += -I"$(JAVA_HOME)/include/darwin"
    LIBS := 
endif

# Source and target settings
SRC_DIR := src/main/native
TARGET_DIR := target/native/$(PLATFORM)/$(ARCH)
SOURCES := $(SRC_DIR)/javaext_time.c
TARGET := $(TARGET_DIR)/$(LIB_PREFIX)javaext_time.$(LIB_EXT)

# Default target
all: $(TARGET)

# Create target directory
$(TARGET_DIR):
	mkdir -p $(TARGET_DIR)

# Build the native library
$(TARGET): $(SOURCES) | $(TARGET_DIR)
	$(CC) $(CFLAGS) $(INCLUDES) $(SHARED_FLAG) -o $@ $< $(LIBS)

# Clean target
clean:
	rm -rf target/native

# Install target (copy to resources)
install: $(TARGET)
	mkdir -p src/main/resources/native/$(PLATFORM)/$(ARCH)
	cp $(TARGET) src/main/resources/native/$(PLATFORM)/$(ARCH)/

# Help target
help:
	@echo "Available targets:"
	@echo "  all      - Build native library for current platform"
	@echo "  clean    - Remove build artifacts"
	@echo "  install  - Copy built library to resources directory"
	@echo "  help     - Show this help message"
	@echo ""
	@echo "Current platform: $(PLATFORM)-$(ARCH)"
	@echo "JAVA_HOME: $(JAVA_HOME)"

# Declare phony targets
.PHONY: all clean install help
