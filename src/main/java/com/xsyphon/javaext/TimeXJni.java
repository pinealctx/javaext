package com.xsyphon.javaext;

/**
 * JNI 方式调用本地 clock_gettime 获取纳秒级 Unix 时间戳
 */
public class TimeXJni {
    static {
        // 根据平台动态加载 so/dylib
        String os = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch").toLowerCase();
        String lib = null;
        if (os.contains("mac")) {
            lib = arch.contains("aarch64") || arch.contains("arm") ? "timexjni_mac_arm64" : "timexjni_mac_x64";
        } else if (os.contains("linux")) {
            lib = arch.contains("aarch64") || arch.contains("arm") ? "timexjni_linux_arm64" : "timexjni_linux_x64";
        }
        if (lib != null) {
            try {
                System.loadLibrary(lib);
            } catch (UnsatisfiedLinkError e) {
                System.err.println("Native lib load failed: " + e);
            }
        }
    }
    public static native long unixNanoByJni();
}
