package com.g2806.tntimer.forge;

import com.g2806.tntimer.TNTimer;
import com.g2806.tntimer.TNTimerHudRenderer;
import com.g2806.tntimer.TNTimerKeys;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(TNTimer.MOD_ID)
public final class TNTimerForge {

    public TNTimerForge(FMLJavaModLoadingContext context) {
        TNTimer.init();

        // "Config" button in the mod list.
        context.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(TNTimer::createConfigScreen));

        context.getModEventBus().addListener((RegisterKeyMappingsEvent event) -> event.register(TNTimerKeys.OPEN_CONFIG));

        // The loader replaces vanilla's Gui.render, so the HUD mixin can't run; draw from its event.
        MinecraftForge.EVENT_BUS.addListener((RenderGuiEvent.Post event) -> {
            if (Minecraft.getInstance().gui.getClass() != Gui.class) TNTimerHudRenderer.render(event.getGuiGraphics());
        });

        MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent.Post event) -> TNTimerKeys.handlePresses());
    }
}
