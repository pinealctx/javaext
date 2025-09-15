package com.xsyphon.javaext;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Structure;

/**
 * 通过JNA调用系统clock_gettime获取纳秒级Unix时间戳
 */
public class TimeXJna {
    public interface CLibrary extends Library {
        CLibrary INSTANCE = Native.load(Platform.isMac() ? "c" : "rt", CLibrary.class);
        int clock_gettime(int clk_id, Timespec tp);
    }
    public static class Timespec extends Structure {
        public long tv_sec;
        public long tv_nsec;
        @Override
        protected java.util.List<String> getFieldOrder() {
            return java.util.Arrays.asList("tv_sec", "tv_nsec");
        }
    }
    // CLOCK_REALTIME = 0, CLOCK_MONOTONIC = 1
    private static final int CLOCK_REALTIME = 0;

    public static long unixNanoByJna() {
        Timespec tp = new Timespec();
        int ret = CLibrary.INSTANCE.clock_gettime(CLOCK_REALTIME, tp);
        if (ret != 0) throw new RuntimeException("clock_gettime failed: " + ret);
        return tp.tv_sec * 1_000_000_000L + tp.tv_nsec;
    }
}
