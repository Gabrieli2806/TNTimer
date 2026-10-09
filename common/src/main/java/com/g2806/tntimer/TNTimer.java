package com.g2806.tntimer;

import com.g2806.tntimer.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loader-agnostic entry point. Each loader module calls {@link #init()} from its own
 * client initialization hook; everything below this point is shared code.
 */
public final class TNTimer {

    public static final String MOD_ID = "tntimer";
    public static final String MOD_NAME = "TNTimer";
    private static final Logger LOGGER = LoggerFactory.getLogger(TNTimer.class);

    private TNTimer() {
    }

    public static void init() {
        TNTimerConfig.getInstance();
        LOGGER.info("{} loaded on {}", MOD_NAME, Services.PLATFORM.getPlatformName());
    }

    /** Opens the config screen over whatever screen is currently shown. */
    public static void openConfigScreen() {
        Minecraft mc = Minecraft.getInstance();
        mc.setScreenAndShow(createConfigScreen(mc.screen));
    }

    public static Screen createConfigScreen(Screen parent) {
        return new TNTimerConfigScreen(parent);
    }
}
