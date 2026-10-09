package com.g2806.tntimer.plugin;

import org.bukkit.configuration.file.FileConfiguration;

/** Snapshot of config.yml. */
final class Settings {

    enum Mode { AUTO, DISPLAY, NAME }

    final boolean enabled;
    final Mode mode;
    final boolean showOnlySeconds;
    final boolean sulfurCubes;
    final boolean background;
    final boolean shadow;
    final boolean seeThrough;
    final float offset;

    Settings(FileConfiguration config) {
        enabled = config.getBoolean("enabled", true);
        Mode parsed;
        try {
            parsed = Mode.valueOf(config.getString("mode", "auto").trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            parsed = Mode.AUTO;
        }
        mode = parsed;
        showOnlySeconds = config.getBoolean("show-only-seconds", false);
        sulfurCubes = config.getBoolean("sulfur-cubes", true);
        background = config.getBoolean("display.background", true);
        shadow = config.getBoolean("display.shadow", true);
        seeThrough = config.getBoolean("display.see-through", true);
        offset = (float) config.getDouble("display.offset", 0.25);
    }
}
