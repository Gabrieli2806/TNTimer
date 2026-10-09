package com.g2806.tntimer;

import com.g2806.tntimer.platform.Services;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.minecraft.client.resources.I18n;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Reader;
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
    private static final Logger LOGGER = LogManager.getLogger(TNTimerConfig.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
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
            return I18n.format(translationKey);
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
            return I18n.format(translationKey);
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

        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            TNTimerConfig loaded = GSON.fromJson(reader, TNTimerConfig.class);
            if (loaded == null) {
                return new TNTimerConfig();
            }
            loaded.sanitize();
            return loaded;
        } catch (IOException | JsonParseException e) {
            // A broken file must not crash the game: keep a copy for the user and use defaults.
            LOGGER.error("Failed to read {}, using defaults", file, e);
            backupBrokenFile(file);
            return new TNTimerConfig();
        }
    }

    private static void backupBrokenFile(Path file) {
        try {
            Files.move(file, file.resolveSibling(FILE_NAME + ".broken"), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            LOGGER.warn("Could not back up broken config {}", file, e);
        }
    }

    /**
     * Repairs values Gson can't validate: unknown enum names become null, and hand-edited
     * numbers can be out of range.
     */
    public void sanitize() {
        TNTimerConfig defaults = new TNTimerConfig();
        if (displayMode == null) displayMode = defaults.displayMode;
        if (position == null) position = defaults.position;
        hudScale = TimerFormat.clamp(hudScale, MIN_HUD_SCALE, MAX_HUD_SCALE);
        maxTntDisplay = TimerFormat.clamp(maxTntDisplay, 1, MAX_TNT_DISPLAY);
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

    public void save() {
        Path file = configFile();
        Path temp = file.resolveSibling(FILE_NAME + ".tmp");
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
            // Write-then-move so a crash mid-save never leaves a half-written config.
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            LOGGER.error("Failed to save {}", file, e);
        }
    }
}
