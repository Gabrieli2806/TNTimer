package com.g2806.tntimer.forge;

import com.g2806.tntimer.TNTimer;
import com.g2806.tntimer.TNTimerKeys;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(TNTimer.MOD_ID)
public final class TNTimerForge {

    public TNTimerForge(FMLJavaModLoadingContext context) {
        TNTimer.init();

        RegisterKeyMappingsEvent.BUS.addListener(event -> event.register(TNTimerKeys.OPEN_CONFIG));

        TickEvent.ClientTickEvent.Post.BUS.addListener(event -> TNTimerKeys.handlePresses());
    }
}
