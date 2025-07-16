package com.g2806.tntimer;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class TNTimerClothConfigScreen {

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("TNTimer Settings"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // Get current configuration
        TNTimerConfig config = TNTimerConfig.getInstance();

        // Single Settings Category - everything in one tab
        ConfigCategory settings = builder.getOrCreateCategory(Text.literal("TNTimer Settings"));

        // Enable/Disable
        settings.addEntry(entryBuilder
                .startBooleanToggle(Text.literal("Enable TNT Timer"), config.enabled)
                .setDefaultValue(true)
                .setTooltip(Text.literal("Enable or disable the TNT countdown timer"))
                .setSaveConsumer(newValue -> config.enabled = newValue)
                .build());

        // Max TNT Display
        settings.addEntry(entryBuilder
                .startIntSlider(Text.literal("Max TNT Display"), config.maxTntDisplay, 1, 10)
                .setDefaultValue(5)
                .setTooltip(Text.literal("Maximum number of TNT entities to display at once"))
                .setSaveConsumer(newValue -> config.maxTntDisplay = newValue)
                .build());

        // Position
        settings.addEntry(entryBuilder
                .startEnumSelector(Text.literal("Position"), TNTimerConfig.Position.class, config.position)
                .setDefaultValue(TNTimerConfig.Position.TOP_LEFT)
                .setTooltip(Text.literal("Screen position for the TNT timer"))
                .setEnumNameProvider(value -> ((TNTimerConfig.Position) value).getDisplayName())
                .setSaveConsumer(newValue -> config.position = newValue)
                .build());

        // Show Only Seconds
        settings.addEntry(entryBuilder
                .startBooleanToggle(Text.literal("Show Only Seconds"), config.showOnlySeconds)
                .setDefaultValue(false)
                .setTooltip(Text.literal("Show only seconds (e.g. '2.5s') instead of full text"))
                .setSaveConsumer(newValue -> config.showOnlySeconds = newValue)
                .build());

        // Show Background
        settings.addEntry(entryBuilder
                .startBooleanToggle(Text.literal("Show Background"), config.showBackground)
                .setDefaultValue(false)
                .setTooltip(Text.literal("Show a semi-transparent background behind the text"))
                .setSaveConsumer(newValue -> config.showBackground = newValue)
                .build());

        // Separator
        settings.addEntry(entryBuilder
                .startTextDescription(Text.literal(""))
                .build());

        // Support section
        settings.addEntry(entryBuilder
                .startTextDescription(Text.literal("💖 Support the Developer"))
                .build());

        settings.addEntry(entryBuilder
                .startTextDescription(Text.literal("Visit: ko-fi.com/gabrieli2806"))
                .build());

        settings.addEntry(entryBuilder
                .startTextDescription(Text.literal(""))
                .build());

        // Credits
        settings.addEntry(entryBuilder
                .startTextDescription(Text.literal("Made by Gabrieli2806"))
                .build());

        // Save configuration when screen is closed
        builder.setSavingRunnable(() -> {
            config.save();
        });

        return builder.build();
    }
}