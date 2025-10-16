package com.g2806.tntimer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class TNTimerConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(TNTimerConfig.class);
    private static TNTimerConfig instance;

    public Position position = Position.TOP_LEFT;
    public int maxTntDisplay = 5;
    public boolean enabled = true;
    public boolean showOnlySeconds = true;
    public boolean showBackground = false;
    public DisplayMode displayMode = DisplayMode.BOTH;
    public boolean show3DBackground = false;

    // Enum for display mode
    public enum DisplayMode {
        BOTH("tntimer.displaymode.both"),
        HUD_ONLY("tntimer.displaymode.hud_only"),
        TIMER_3D_ONLY("tntimer.displaymode.3d_only");

        private final String translationKey;

        DisplayMode(String translationKey) {
            this.translationKey = translationKey;
        }

        public Component getDisplayName() {
            return Component.translatable(translationKey);
        }

        @Override
        public String toString() {
            return Component.translatable(translationKey).getString();
        }
    }

    // Enum for screen positions
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

        public Component getDisplayName() {
            return Component.translatable(translationKey);
        }

        @Override
        public String toString() {
            return Component.translatable(translationKey).getString();
        }
    }

    // Singleton pattern for global access
    public static TNTimerConfig getInstance() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    // Load configuration from file
    public static TNTimerConfig load() {
        File configFile = new File(Minecraft.getInstance().gameDirectory, "config/tntimer.json");
        Gson gson = new Gson();
        TNTimerConfig config = new TNTimerConfig();

        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                TNTimerConfig loaded = gson.fromJson(reader, TNTimerConfig.class);
                if (loaded != null) {
                    config = loaded;
                }
            } catch (IOException e) {
                LOGGER.error("Failed to load config file: {}", configFile.getAbsolutePath(), e);
            }
        }

        return config;
    }

    // Save configuration to file
    public void save() {
        File configDir = new File(Minecraft.getInstance().gameDirectory, "config");
        File configFile = new File(configDir, "tntimer.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        if (!configDir.exists()) {
            if (!configDir.mkdirs()) {
                LOGGER.error("Failed to create config directory: {}", configDir.getAbsolutePath());
                return;
            }
        }

        try (FileWriter writer = new FileWriter(configFile)) {
            gson.toJson(this, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to save config file: {}", configFile.getAbsolutePath(), e);
        }
    }
}