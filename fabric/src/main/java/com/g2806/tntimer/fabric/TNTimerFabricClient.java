package com.g2806.tntimer.fabric;

import com.g2806.tntimer.TNTimer;
import com.g2806.tntimer.TNTimerKeys;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;

public class TNTimerFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        TNTimer.init();

        KeyMappingHelper.registerKeyMapping(TNTimerKeys.OPEN_CONFIG);
        ClientTickEvents.END_CLIENT_TICK.register(client -> TNTimerKeys.handlePresses());
    }
}
