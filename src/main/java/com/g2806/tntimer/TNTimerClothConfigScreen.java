package com.g2806.tntimer;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TNTimerClothConfigScreen {

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("tntimer.config.title"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // Get current configuration
        TNTimerConfig config = TNTimerConfig.getInstance();

        // Single Settings Category - everything in one tab
        ConfigCategory settings = builder.getOrCreateCategory(Component.translatable("tntimer.config.category"));

        // Enable/Disable
        settings.addEntry(entryBuilder
                .startBooleanToggle(Component.translatable("tntimer.config.enable"), config.enabled)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("tntimer.config.enable.tooltip"))
                .setSaveConsumer(newValue -> config.enabled = newValue)
                .build());

        // Max TNT Display
        settings.addEntry(entryBuilder
                .startIntSlider(Component.translatable("tntimer.config.max_tnt_display"), config.maxTntDisplay, 1, 10)
                .setDefaultValue(5)
                .setTooltip(Component.translatable("tntimer.config.max_tnt_display.tooltip"))
                .setSaveConsumer(newValue -> config.maxTntDisplay = newValue)
                .build());

        // Position
        settings.addEntry(entryBuilder
                .startEnumSelector(Component.translatable("tntimer.config.position_label"), TNTimerConfig.Position.class, config.position)
                .setDefaultValue(TNTimerConfig.Position.TOP_LEFT)
                .setTooltip(Component.translatable("tntimer.config.position_label.tooltip"))
                .setEnumNameProvider(value -> ((TNTimerConfig.Position) value).getDisplayName())
                .setSaveConsumer(newValue -> config.position = newValue)
                .build());

        // Show Background
        settings.addEntry(entryBuilder
                .startBooleanToggle(Component.translatable("tntimer.config.show_hud_background"), config.showBackground)
                .setDefaultValue(false)
                .setTooltip(Component.translatable("tntimer.config.show_hud_background.tooltip"))
                .setSaveConsumer(newValue -> config.showBackground = newValue)
                .build());

        // Display Mode
        settings.addEntry(entryBuilder
                .startEnumSelector(Component.translatable("tntimer.config.display_mode_label"), TNTimerConfig.DisplayMode.class, config.displayMode)
                .setDefaultValue(TNTimerConfig.DisplayMode.BOTH)
                .setTooltip(Component.translatable("tntimer.config.display_mode_label.tooltip"))
                .setEnumNameProvider(value -> ((TNTimerConfig.DisplayMode) value).getDisplayName())
                .setSaveConsumer(newValue -> config.displayMode = newValue)
                .build());

        // Separator
        settings.addEntry(entryBuilder
                .startTextDescription(Component.literal(""))
                .build());

        // Support section
        settings.addEntry(entryBuilder
                .startTextDescription(Component.literal("💖 Support the Developer"))
                .build());

        settings.addEntry(entryBuilder
                .startTextDescription(Component.literal("Visit: ko-fi.com/gabrieli2806"))
                .build());

        settings.addEntry(entryBuilder
                .startTextDescription(Component.literal(""))
                .build());

        // Credits
        settings.addEntry(entryBuilder
                .startTextDescription(Component.literal("Made by Gabrieli2806"))
                .build());

        // Save configuration when screen is closed
        builder.setSavingRunnable(() -> {
            config.save();
        });

        return builder.build();
    }
}