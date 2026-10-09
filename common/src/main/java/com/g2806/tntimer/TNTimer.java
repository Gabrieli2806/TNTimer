package com.g2806.tntimer;

import com.g2806.tntimer.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Loader-agnostic entry point. The loader module calls {@link #init()} from its own
 * initialization hook and forwards render/tick events here; everything else is shared.
 */
public final class TNTimer {

    public static final String MOD_ID = "tntimer";
    public static final String MOD_NAME = "TNTimer";
    private static final Logger LOGGER = LogManager.getLogger(TNTimer.class);

    private TNTimer() {
    }

    public static void init() {
        TNTimerConfig.getInstance();
        LOGGER.info("{} loaded on {}", MOD_NAME, Services.PLATFORM.getPlatformName());
    }

    /** Opens the config screen over whatever screen is currently shown. */
    public static void openConfigScreen() {
        Minecraft mc = Minecraft.getMinecraft();
        mc.displayGuiScreen(createConfigScreen(mc.currentScreen));
    }

    public static GuiScreen createConfigScreen(GuiScreen parent) {
        return new TNTimerConfigScreen(parent);
    }
}
