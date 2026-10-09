package com.g2806.tntimer.fabric;

import com.g2806.tntimer.TNTimer;
import io.github.prospector.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.screens.Screen;

import java.util.function.Function;

public class TNTimerModMenuIntegration implements ModMenuApi {

    @Override
    public String getModId() {
        return TNTimer.MOD_ID;
    }

    @Override
    public Function<Screen, ? extends Screen> getConfigScreenFactory() {
        return TNTimer::createConfigScreen;
    }
}
