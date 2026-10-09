package com.g2806.tntimer.neoforge;

import com.g2806.tntimer.TNTimer;
import com.g2806.tntimer.TNTimerKeys;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.ConfigScreenHandler;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TickEvent;

@Mod(TNTimer.MOD_ID)
public class TNTimerNeoForge {

    public TNTimerNeoForge(IEventBus modBus) {
        // NeoForge 20.4's @Mod has no dist filter; the mod is client-only.
        if (!FMLEnvironment.dist.isClient()) return;

        TNTimer.init();

        // "Config" button in the mod list.
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((mc, parent) -> TNTimer.createConfigScreen(parent)));

        modBus.addListener(RegisterKeyMappingsEvent.class, event -> event.register(TNTimerKeys.OPEN_CONFIG));

        NeoForge.EVENT_BUS.addListener(TickEvent.ClientTickEvent.class, event -> {
            if (event.phase == TickEvent.Phase.END) TNTimerKeys.handlePresses();
        });
    }
}
