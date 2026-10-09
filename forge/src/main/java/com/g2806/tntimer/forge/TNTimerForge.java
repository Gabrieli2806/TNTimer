package com.g2806.tntimer.forge;

import com.g2806.tntimer.TNTimer;
import com.g2806.tntimer.TNTimerKeys;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(TNTimer.MOD_ID)
public final class TNTimerForge {

    public TNTimerForge() {
        // Client-only mod; do nothing if a server loads it.
        if (!FMLEnvironment.dist.isClient()) return;

        TNTimer.init();

        // "Config" button in the mod list.
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY,
                () -> (mc, parent) -> TNTimer.createConfigScreen(parent));

        // Key mappings are registered during client setup before 1.19.
        FMLJavaModLoadingContext.get().getModEventBus()
                .addListener((FMLClientSetupEvent event) -> ClientRegistry.registerKeyBinding(TNTimerKeys.OPEN_CONFIG));

        MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) TNTimerKeys.handlePresses();
        });
    }
}
