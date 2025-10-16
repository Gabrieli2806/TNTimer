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
                .setTitle(Component.literal("TNTimer Settings"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // Get current configuration
        TNTimerConfig config = TNTimerConfig.getInstance();

        // Single Settings Category - everything in one tab
        ConfigCategory settings = builder.getOrCreateCategory(Component.literal("TNTimer Settings"));

        // Enable/Disable
        settings.addEntry(entryBuilder
                .startBooleanToggle(Component.literal("Enable TNT Timer"), config.enabled)
                .setDefaultValue(true)
                .setTooltip(Component.literal("Enable or disable the TNT countdown timer"))
                .setSaveConsumer(newValue -> config.enabled = newValue)
                .build());

        // Max TNT Display
        settings.addEntry(entryBuilder
                .startIntSlider(Component.literal("Max TNT Display"), config.maxTntDisplay, 1, 10)
                .setDefaultValue(5)
                .setTooltip(Component.literal("Maximum number of TNT entities to display at once"))
                .setSaveConsumer(newValue -> config.maxTntDisplay = newValue)
                .build());

        // Position
        settings.addEntry(entryBuilder
                .startEnumSelector(Component.literal("Position"), TNTimerConfig.Position.class, config.position)
                .setDefaultValue(TNTimerConfig.Position.TOP_LEFT)
                .setTooltip(Component.literal("Screen position for the TNT timer"))
                .setEnumNameProvider(value -> ((TNTimerConfig.Position) value).getDisplayName())
                .setSaveConsumer(newValue -> config.position = newValue)
                .build());

        // Show Only Seconds
        settings.addEntry(entryBuilder
                .startBooleanToggle(Component.literal("Show Only Seconds"), config.showOnlySeconds)
                .setDefaultValue(false)
                .setTooltip(Component.literal("Show only seconds (e.g. '2.5s') instead of full text"))
                .setSaveConsumer(newValue -> config.showOnlySeconds = newValue)
                .build());

        // Show Background
        settings.addEntry(entryBuilder
                .startBooleanToggle(Component.literal("Show Background"), config.showBackground)
                .setDefaultValue(false)
                .setTooltip(Component.literal("Show a semi-transparent background behind the text"))
                .setSaveConsumer(newValue -> config.showBackground = newValue)
                .build());

        // Enable 3D Timer
        settings.addEntry(entryBuilder
                .startBooleanToggle(Component.literal("Enable 3D Timer"), config.enable3DTimer)
                .setDefaultValue(true)
                .setTooltip(Component.literal("Show timer above TNT entities in 3D world (like nametags)"))
                .setSaveConsumer(newValue -> config.enable3DTimer = newValue)
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