package com.g2806.tntimer;

import java.util.Locale;

/** Text and colour for a fuse, shared by the HUD and the 3D labels so they always match. */
public final class TimerFormat {

    private static final int TICKS_PER_SECOND = 20;

    public static final int COLOR_SAFE = 0xFFFFFFFF;    // white, 2s or more
    public static final int COLOR_WARNING = 0xFFFF8000; // orange, under 2s
    public static final int COLOR_DANGER = 0xFFFF0000;  // red, under 1s

    private TimerFormat() {
    }

    /** "3.4s" or "TNT: 3.4s". Locale.ROOT keeps the decimal point a dot in every language. */
    public static String format(int fuseTicks, boolean onlySeconds) {
        String seconds = String.format(Locale.ROOT, "%.1fs", Math.max(0, fuseTicks) / (double) TICKS_PER_SECOND);
        return onlySeconds ? seconds : "TNT: " + seconds;
    }

    /** ARGB colour for the remaining fuse. */
    public static int color(int fuseTicks) {
        if (fuseTicks < TICKS_PER_SECOND) return COLOR_DANGER;
        if (fuseTicks < 2 * TICKS_PER_SECOND) return COLOR_WARNING;
        return COLOR_SAFE;
    }

    /** Version-independent clamp (MathHelper's clamp methods were renamed across MCP releases). */
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
