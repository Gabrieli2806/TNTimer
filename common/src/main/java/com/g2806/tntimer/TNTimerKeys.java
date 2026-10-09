package com.g2806.tntimer;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;

/**
 * The mod's key mappings. Built from vanilla types only; each loader module registers
 * them with its own API and polls them from its own client tick hook.
 */
public final class TNTimerKeys {

    public static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(ResourceLocation.fromNamespaceAndPath(TNTimer.MOD_ID, "general"));

    public static final KeyMapping OPEN_CONFIG = new KeyMapping(
            "key.tntimer.open_config",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_K,
            CATEGORY);

    private TNTimerKeys() {
    }

    /** Opens the config screen for every queued press of the keybind. */
    public static void handlePresses() {
        while (OPEN_CONFIG.consumeClick()) {
            TNTimer.openConfigScreen();
        }
    }
}
