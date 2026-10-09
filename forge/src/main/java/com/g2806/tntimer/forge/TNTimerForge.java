package com.g2806.tntimer.forge;

import com.g2806.tntimer.TNTWorldRenderer;
import com.g2806.tntimer.TNTimer;
import com.g2806.tntimer.TNTimerHudRenderer;
import com.g2806.tntimer.TNTimerKeys;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * Forge glue for legacy Minecraft. Forge here has no Mixin, so the HUD and 3D labels hook
 * Forge's render events instead of the shared mixins used on 1.16.5+.
 */
@Mod(modid = TNTimer.MOD_ID, useMetadata = true, clientSideOnly = true,
        guiFactory = "com.g2806.tntimer.forge.TNTimerGuiFactory")
public final class TNTimerForge {

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ForgePlatformHelper.configDirectory = event.getModConfigurationDirectory().toPath();
        TNTimer.init();
        ClientRegistry.registerKeyBinding(TNTimerKeys.OPEN_CONFIG);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type == RenderGameOverlayEvent.ElementType.ALL) {
            TNTimerHudRenderer.render();
        }
    }

    @SubscribeEvent
    public void onWorldRendered(RenderWorldLastEvent event) {
        TNTWorldRenderer.render(event.partialTicks);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            TNTimerKeys.handlePresses();
        }
    }
}
