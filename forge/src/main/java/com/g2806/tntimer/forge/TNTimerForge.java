package com.g2806.tntimer.forge;

import com.g2806.tntimer.FusedEntities;
import com.g2806.tntimer.TNTWorldRenderer;
import com.g2806.tntimer.TNTimer;
import com.g2806.tntimer.TNTimerHudRenderer;
import com.g2806.tntimer.TNTimerKeys;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
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

        // Forge 1.14 has no Mixin: draw the HUD and the 3D label from Forge's own events.
        MinecraftForge.EVENT_BUS.addListener((RenderGameOverlayEvent.Post event) -> {
            if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) TNTimerHudRenderer.render();
        });
        MinecraftForge.EVENT_BUS.addListener((RenderWorldLastEvent event) -> renderLabels(event.getPartialTicks()));
    }

    /** Vanilla-style nametag over each labelled TNT, drawn after the world (camera-relative). */
    private static void renderLabels(float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
        for (Entity entity : FusedEntities.collect(mc.level)) {
            String label = TNTWorldRenderer.labelFor(entity);
            if (label == null) continue;
            double x = Mth.lerp(partialTick, entity.xOld, entity.x) - camera.x;
            double y = Mth.lerp(partialTick, entity.yOld, entity.y) - camera.y + entity.getBbHeight() + 0.5;
            double z = Mth.lerp(partialTick, entity.zOld, entity.z) - camera.z;
            GameRenderer.renderNameTagInWorld(mc.font, label, (float) x, (float) y, (float) z, 0,
                    dispatcher.playerRotY, dispatcher.playerRotX, false);
        }
    }
}
