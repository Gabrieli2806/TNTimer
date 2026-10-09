package com.g2806.tntimer;

import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.glfw.GLFW;

/** The mod's key bindings; the loader registers them and calls {@link #handlePresses()} each tick. */
public final class TNTimerKeys {

    public static final KeyBinding OPEN_CONFIG = new KeyBinding(
            "key.tntimer.open_config", GLFW.GLFW_KEY_K, "key.category.tntimer.general");

    private TNTimerKeys() {
    }

    /** Opens the config screen for every queued press of the key. */
    public static void handlePresses() {
        while (OPEN_CONFIG.isPressed()) {
            TNTimer.openConfigScreen();
        }
    }
}
