package com.g2806.tntimer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.TextFormatting;

import java.util.Comparator;
import java.util.List;

/**
 * Renders TNT countdown timers as nametag-style labels above active TNT, drawn with vanilla's
 * own nameplate routine after the world has rendered.
 */
public final class TNTWorldRenderer {

    private static final double VERTICAL_OFFSET = 0.5;
    /** Vanilla only draws nametags within 64 blocks; match it. */
    private static final double MAX_DISTANCE_SQR = 64.0 * 64.0;

    private TNTWorldRenderer() {
    }

    public static void render(float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null || mc.gameSettings.hideGUI) return;

        TNTimerConfig config = TNTimerConfig.getInstance();
        if (!config.enabled || config.displayMode != TNTimerConfig.DisplayMode.WORLD) return;

        RenderManager rm = mc.getRenderManager();
        if (rm.renderViewEntity == null) return;
        final double camX = rm.viewerPosX;
        final double camY = rm.viewerPosY;
        final double camZ = rm.viewerPosZ;

        List<Entity> entities = FusedEntities.collect(mc.world);
        entities.removeIf(e -> e.getDistanceSq(camX, camY, camZ) > MAX_DISTANCE_SQR);
        if (entities.isEmpty()) return;

        // Closest first, so the max-timers limit keeps the labels that matter most.
        entities.sort(Comparator.comparingDouble(e -> e.getDistanceSq(camX, camY, camZ)));

        boolean thirdPersonFrontal = mc.gameSettings.thirdPersonView == 2;
        int displayCount = Math.min(entities.size(), config.maxTntDisplay);
        for (int i = 0; i < displayCount; i++) {
            Entity entity = entities.get(i);
            int fuse = FusedEntities.fuseOf(entity);
            String label = colorCode(fuse) + TimerFormat.format(fuse, config.showOnlySeconds);

            // Interpolated position, so labels on moving TNT don't jitter.
            float x = (float) (entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks - camX);
            float y = (float) (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks - camY);
            float z = (float) (entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks - camZ);

            EntityRenderer.drawNameplate(mc.fontRenderer, label, x, y + entity.height + (float) VERTICAL_OFFSET, z,
                    0, rm.playerViewY, rm.playerViewX, thirdPersonFrontal, false);
        }
    }

    /** Nameplates take formatting codes, not RGB: white, gold and red match the HUD colours. */
    private static String colorCode(int fuse) {
        int color = TimerFormat.color(fuse);
        if (color == TimerFormat.COLOR_DANGER) return TextFormatting.RED.toString();
        if (color == TimerFormat.COLOR_WARNING) return TextFormatting.GOLD.toString();
        return TextFormatting.WHITE.toString();
    }
}
