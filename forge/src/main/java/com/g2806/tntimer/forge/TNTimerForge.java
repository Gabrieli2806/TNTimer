package com.g2806.tntimer.forge;

import com.g2806.tntimer.TNTWorldRenderer;
import com.g2806.tntimer.TNTimer;
import com.g2806.tntimer.TNTimerHudRenderer;
import com.g2806.tntimer.TNTimerKeys;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

/**
 * Forge glue for 1.13. Forge here has no Mixin, so the HUD and 3D labels hook Forge's
 * render events instead of the shared mixins used on newer versions.
 */
@Mod(TNTimer.MOD_ID)
public final class TNTimerForge {

    public TNTimerForge() {
        // Client-only mod; do nothing if a server loads it.
        if (!FMLEnvironment.dist.isClient()) return;

        TNTimer.init();

        // "Config" button in the mod list.
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY,
                () -> (mc, parent) -> TNTimer.createConfigScreen(parent));

        FMLJavaModLoadingContext.get().getModEventBus()
                .addListener((FMLClientSetupEvent event) -> ClientRegistry.registerKeyBinding(TNTimerKeys.OPEN_CONFIG));

        MinecraftForge.EVENT_BUS.addListener((RenderGameOverlayEvent.Post event) -> {
            if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) TNTimerHudRenderer.render();
        });
        MinecraftForge.EVENT_BUS.addListener((RenderWorldLastEvent event) -> TNTWorldRenderer.render(event.getPartialTicks()));
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) TNTimerKeys.handlePresses();
        });
    }
}
