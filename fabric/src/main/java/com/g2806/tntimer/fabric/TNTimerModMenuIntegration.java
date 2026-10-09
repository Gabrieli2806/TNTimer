package com.g2806.tntimer.fabric;

import com.g2806.tntimer.TNTimer;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class TNTimerModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return TNTimer::createConfigScreen;
    }
}
