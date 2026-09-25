package com.g2806.tntimer.neoforge;

import com.g2806.tntimer.TNTimer;
import com.g2806.tntimer.TNTimerKeys;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = TNTimer.MOD_ID, dist = Dist.CLIENT)
public class TNTimerNeoForge {

    public TNTimerNeoForge(IEventBus modBus) {
        TNTimer.init();

        modBus.addListener(RegisterKeyMappingsEvent.class,
                event -> event.register(TNTimerKeys.OPEN_CONFIG));

        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class,
                event -> TNTimerKeys.handlePresses());
    }
}
