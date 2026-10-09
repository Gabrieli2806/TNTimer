package com.g2806.tntimer.plugin;

import org.bukkit.ChatColor;

import java.util.Locale;

/** Same text and colours as the TNTimer mod: white, then gold under 2s, red under 1s. */
final class TimerFormat {

    private static final int TICKS_PER_SECOND = 20;

    private TimerFormat() {
    }

    static String format(int fuseTicks, boolean onlySeconds) {
        String seconds = String.format(Locale.ROOT, "%.1fs", Math.max(0, fuseTicks) / (double) TICKS_PER_SECOND);
        return color(fuseTicks) + (onlySeconds ? seconds : "TNT: " + seconds);
    }

    private static ChatColor color(int fuseTicks) {
        if (fuseTicks < TICKS_PER_SECOND) return ChatColor.RED;
        if (fuseTicks < 2 * TICKS_PER_SECOND) return ChatColor.GOLD;
        return ChatColor.WHITE;
    }
}
