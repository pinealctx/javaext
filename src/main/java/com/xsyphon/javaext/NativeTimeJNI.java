package com.xsyphon.javaext;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * JNI interface for native clock_gettime implementation.
 * Provides high-performance access to system-level time functions
 * on supported platforms (Linux and macOS with AMD64/ARM64).
 * 
 * @author pinealctx
 * @version 1.0.0
 */
public class NativeTimeJNI {
    
    private static boolean libraryLoaded = false;
    private static String loadError = null;
    
    static {
        try {
            loadNativeLibrary();
            libraryLoaded = true;
        } catch (Exception e) {
            loadError = e.getMessage();
            libraryLoaded = false;
        }
    }
    
    /**
     * Load the appropriate native library for the current platform.
     * 
     * @throws RuntimeException if library loading fails
     */
    private static void loadNativeLibrary() throws RuntimeException {
        String osName = System.getProperty("os.name").toLowerCase();
        String osArch = System.getProperty("os.arch").toLowerCase();
        
        String libraryName = getNativeLibraryName(osName, osArch);
        if (libraryName == null) {
            throw new RuntimeException("Unsupported platform: " + osName + " " + osArch);
        }
        
        try {
            // Try to load from java.library.path first
            System.loadLibrary("javaext_time");
        } catch (UnsatisfiedLinkError e1) {
            try {
                // Try to load from classpath resources
                loadLibraryFromResources(libraryName);
            } catch (Exception e2) {
                throw new RuntimeException("Failed to load native library: " + e2.getMessage(), e2);
            }
        }
    }
    
    /**
     * Get the native library name for the given platform.
     * 
     * @param osName operating system name
     * @param osArch processor architecture
     * @return library name or null if unsupported
     */
    private static String getNativeLibraryName(String osName, String osArch) {
        String arch = normalizeArch(osArch);
        if (arch == null) {
            return null;
        }
        
        if (osName.contains("linux")) {
            return "native/linux/" + arch + "/libjavaext_time.so";
        } else if (osName.contains("mac") || osName.contains("darwin")) {
            return "native/macos/" + arch + "/libjavaext_time.dylib";
        }
        
        return null;
    }
    
    /**
     * Normalize processor architecture name.
     * 
     * @param osArch raw architecture string
     * @return normalized architecture name or null if unsupported
     */
    private static String normalizeArch(String osArch) {
        if (osArch.contains("amd64") || osArch.contains("x86_64") || osArch.contains("x64")) {
            return "amd64";
        } else if (osArch.contains("aarch64") || osArch.contains("arm64")) {
            return "arm64";
        }
        return null;
    }
    
    /**
     * Load native library from classpath resources.
     * 
     * @param libraryName library resource path
     * @throws Exception if loading fails
     */
    private static void loadLibraryFromResources(String libraryName) throws Exception {
        InputStream is = NativeTimeJNI.class.getClassLoader().getResourceAsStream(libraryName);
        if (is == null) {
            throw new RuntimeException("Native library not found in resources: " + libraryName);
        }
        
        // Create temporary file
        String[] parts = libraryName.split("/");
        String fileName = parts[parts.length - 1];
        Path tempFile = Files.createTempFile("javaext_time_", "_" + fileName);
        
        try {
            // Copy library to temporary file
            Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
            
            // Load the library
            System.load(tempFile.toAbsolutePath().toString());
            
        } finally {
            try {
                is.close();
            } catch (Exception ignored) {
            }
            
            // Schedule cleanup
            tempFile.toFile().deleteOnExit();
        }
    }
    
    /**
     * Check if the native library is available and loaded.
     * 
     * @return true if native library is available
     */
    public static boolean isAvailable() {
        return libraryLoaded;
    }
    
    /**
     * Get the error message if library loading failed.
     * 
     * @return error message or null if no error
     */
    public static String getLoadError() {
        return loadError;
    }
    
    /**
     * Native method to get Unix nanosecond timestamp using clock_gettime.
     * 
     * @return Unix nanosecond timestamp
     * @throws RuntimeException if native call fails
     */
    public static long clockGetTime() {
        if (!libraryLoaded) {
            throw new RuntimeException("Native library not loaded: " + loadError);
        }
        return clockGetTimeNative();
    }
    
    /**
     * Native method implementation - will be implemented in C.
     * Uses clock_gettime(CLOCK_REALTIME) to get high-precision timestamp.
     * 
     * @return Unix nanosecond timestamp
     */
    private static native long clockGetTimeNative();
}
