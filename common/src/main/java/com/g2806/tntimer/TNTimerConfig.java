package com.g2806.tntimer;

import com.g2806.tntimer.platform.Services;
import net.minecraft.util.StringTranslate;
import net.minecraft.util.MathHelper;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * User settings, stored as JSON in the loader's config directory (config/tntimer.json).
 * Public fields are the serialized format; keep their names stable so old files keep loading.
 */
public class TNTimerConfig {
    private static final Logger LOGGER = Logger.getLogger(TNTimerConfig.class.getName());
    private static final String FILE_NAME = "tntimer.json";
    private static TNTimerConfig instance;

    public boolean enabled = true;
    public DisplayMode displayMode = DisplayMode.WORLD;
    public Position position = Position.TOP_LEFT;
    public int maxTntDisplay = 5;
    public boolean showOnlySeconds = true;
    public boolean showBackground = false;
    public float hudScale = 1.0f;

    public static final float MIN_HUD_SCALE = 0.5f;
    public static final float MAX_HUD_SCALE = 3.0f;
    public static final int MAX_TNT_DISPLAY = 20;

    /** HUD overlay or 3D nametag above the entity. */
    public enum DisplayMode {
        HUD("tntimer.display_mode.hud"),
        WORLD("tntimer.display_mode.world");

        private final String translationKey;

        DisplayMode(String translationKey) {
            this.translationKey = translationKey;
        }

        public String getDisplayName() {
            return StringTranslate.getInstance().translateKey(translationKey);
        }
    }

    /** Where the HUD timer list is anchored. */
    public enum Position {
        TOP_LEFT("tntimer.position.top_left"),
        TOP_RIGHT("tntimer.position.top_right"),
        BOTTOM_LEFT("tntimer.position.bottom_left"),
        BOTTOM_RIGHT("tntimer.position.bottom_right"),
        TOP_CENTER("tntimer.position.top_center"),
        BOTTOM_CENTER("tntimer.position.bottom_center"),
        UNDER_CURSOR("tntimer.position.under_cursor");

        private final String translationKey;

        Position(String translationKey) {
            this.translationKey = translationKey;
        }

        public String getDisplayName() {
            return StringTranslate.getInstance().translateKey(translationKey);
        }
    }

    public static TNTimerConfig getInstance() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    private static Path configFile() {
        return Services.PLATFORM.getConfigDirectory().resolve(FILE_NAME);
    }

    private static TNTimerConfig load() {
        Path file = configFile();
        if (!Files.exists(file)) {
            return new TNTimerConfig();
        }

        try {
            TNTimerConfig loaded = fromJson(new String(Files.readAllBytes(file), StandardCharsets.UTF_8));
            loaded.sanitize();
            return loaded;
        } catch (IOException | RuntimeException e) {
            // A broken file must not crash the game: keep a copy for the user and use defaults.
            LOGGER.log(Level.SEVERE, "Failed to read " + file + ", using defaults", e);
            backupBrokenFile(file);
            return new TNTimerConfig();
        }
    }

    private static void backupBrokenFile(Path file) {
        try {
            Files.move(file, file.resolveSibling(FILE_NAME + ".broken"), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not back up broken config " + file, e);
        }
    }

    /**
     * Repairs values the parser can't validate: unknown enum names become null, and hand-edited
     * numbers can be out of range.
     */
    public void sanitize() {
        TNTimerConfig defaults = new TNTimerConfig();
        if (displayMode == null) displayMode = defaults.displayMode;
        if (position == null) position = defaults.position;
        hudScale = MathHelper.clamp_float(hudScale, MIN_HUD_SCALE, MAX_HUD_SCALE);
        maxTntDisplay = MathHelper.clamp_int(maxTntDisplay, 1, MAX_TNT_DISPLAY);
    }

    public TNTimerConfig copy() {
        TNTimerConfig copy = new TNTimerConfig();
        copy.copyFrom(this);
        return copy;
    }

    public void copyFrom(TNTimerConfig other) {
        enabled = other.enabled;
        displayMode = other.displayMode;
        position = other.position;
        maxTntDisplay = other.maxTntDisplay;
        showOnlySeconds = other.showOnlySeconds;
        showBackground = other.showBackground;
        hudScale = other.hudScale;
    }

    /** Minecraft 1.5 ships no Gson, so the flat config object is (de)serialized by hand. */
    private static final Pattern JSON_ENTRY = Pattern.compile("\"(\\w+)\"\\s*:\\s*(\"([^\"]*)\"|[^,}\\s]+)");

    static TNTimerConfig fromJson(String json) {
        Map<String, String> values = new HashMap<>();
        Matcher matcher = JSON_ENTRY.matcher(json);
        while (matcher.find()) {
            values.put(matcher.group(1), matcher.group(3) != null ? matcher.group(3) : matcher.group(2));
        }
        TNTimerConfig config = new TNTimerConfig();
        if (values.containsKey("enabled")) config.enabled = Boolean.parseBoolean(values.get("enabled"));
        if (values.containsKey("displayMode")) config.displayMode = enumOrNull(DisplayMode.class, values.get("displayMode"));
        if (values.containsKey("position")) config.position = enumOrNull(Position.class, values.get("position"));
        if (values.containsKey("maxTntDisplay")) config.maxTntDisplay = Integer.parseInt(values.get("maxTntDisplay"));
        if (values.containsKey("showOnlySeconds")) config.showOnlySeconds = Boolean.parseBoolean(values.get("showOnlySeconds"));
        if (values.containsKey("showBackground")) config.showBackground = Boolean.parseBoolean(values.get("showBackground"));
        if (values.containsKey("hudScale")) config.hudScale = Float.parseFloat(values.get("hudScale"));
        return config;
    }

    private static <E extends Enum<E>> E enumOrNull(Class<E> type, String name) {
        try {
            return Enum.valueOf(type, name);
        } catch (IllegalArgumentException e) {
            return null; // sanitize() restores the default
        }
    }

    String toJson() {
        return "{\n"
                + "  \"enabled\": " + enabled + ",\n"
                + "  \"displayMode\": \"" + displayMode.name() + "\",\n"
                + "  \"position\": \"" + position.name() + "\",\n"
                + "  \"maxTntDisplay\": " + maxTntDisplay + ",\n"
                + "  \"showOnlySeconds\": " + showOnlySeconds + ",\n"
                + "  \"showBackground\": " + showBackground + ",\n"
                + "  \"hudScale\": " + hudScale + "\n"
                + "}\n";
    }

    public void save() {
        Path file = configFile();
        Path temp = file.resolveSibling(FILE_NAME + ".tmp");
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
                writer.write(toJson());
            }
            // Write-then-move so a crash mid-save never leaves a half-written config.
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to save " + file, e);
        }
    }
}
