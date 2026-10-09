package com.g2806.tntimer;

import com.g2806.tntimer.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import java.util.logging.Logger;

/**
 * Loader-agnostic entry point. The loader module calls {@link #init()} from its own
 * initialization hook and forwards render/tick events here; everything else is shared.
 */
public final class TNTimer {

    public static final String MOD_ID = "tntimer";
    public static final String MOD_NAME = "TNTimer";
    private static final Logger LOGGER = Logger.getLogger(TNTimer.class.getName());

    private TNTimer() {
    }

    public static void init() {
        TNTimerConfig.getInstance();
        LOGGER.info(MOD_NAME + " loaded on " + Services.PLATFORM.getPlatformName());
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
