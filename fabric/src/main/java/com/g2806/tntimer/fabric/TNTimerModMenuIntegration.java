package com.g2806.tntimer.fabric;

import com.g2806.tntimer.TNTimer;
import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;

public class TNTimerModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return TNTimer::createConfigScreen;
    }
}
