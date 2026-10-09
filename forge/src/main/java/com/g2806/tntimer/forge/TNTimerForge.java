package com.g2806.tntimer.forge;

import com.g2806.tntimer.TNTimer;
import com.g2806.tntimer.TNTimerHudRenderer;
import com.g2806.tntimer.TNTimerKeys;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fmlclient.registry.ClientRegistry;
import net.minecraftforge.fmlclient.ConfigGuiHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.ModLoadingContext;
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
        ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class,
                () -> new ConfigGuiHandler.ConfigGuiFactory((mc, parent) -> TNTimer.createConfigScreen(parent)));

        // 1.18 has no RegisterKeyMappingsEvent; key mappings are registered during client setup.
        FMLJavaModLoadingContext.get().getModEventBus()
                .addListener((FMLClientSetupEvent event) -> ClientRegistry.registerKeyBinding(TNTimerKeys.OPEN_CONFIG));

        // The loader replaces vanilla's Gui.render, so the HUD mixin can't run; draw from its event.
        MinecraftForge.EVENT_BUS.addListener((RenderGameOverlayEvent.Post event) -> {
            if (event.getType() == RenderGameOverlayEvent.ElementType.ALL && Minecraft.getInstance().gui.getClass() != Gui.class) TNTimerHudRenderer.render(event.getMatrixStack());
        });

        MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) TNTimerKeys.handlePresses();
        });
    }
}
