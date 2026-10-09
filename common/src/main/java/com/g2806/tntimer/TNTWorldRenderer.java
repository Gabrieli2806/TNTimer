package com.g2806.tntimer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

import java.util.Comparator;
import java.util.List;

/**
 * Renders TNT countdown timers as nametag-style labels above active TNT after the world has
 * rendered, drawn the same way as vanilla's Render.renderLivingLabel.
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

            drawLabel(mc.fontRenderer, label, x, y + entity.height + (float) VERTICAL_OFFSET, z,
                    rm.playerViewY, rm.playerViewX, thirdPersonFrontal);
        }
    }

    private static void drawLabel(FontRenderer font, String text, float x, float y, float z,
                                  float viewerYaw, float viewerPitch, boolean thirdPersonFrontal) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-viewerYaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate((thirdPersonFrontal ? -1 : 1) * viewerPitch, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-0.025F, -0.025F, 0.025F);
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        int halfWidth = font.getStringWidth(text) / 2;
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer buffer = tessellator.getBuffer();
        GlStateManager.disableTexture2D();
        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(-halfWidth - 1, -1, 0).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        buffer.pos(-halfWidth - 1, 8, 0).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        buffer.pos(halfWidth + 1, 8, 0).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        buffer.pos(halfWidth + 1, -1, 0).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();

        // Faint pass visible through blocks, then the solid pass on top, like a vanilla nametag.
        font.drawString(text, -halfWidth, 0, 0x20FFFFFF);
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        font.drawString(text, -halfWidth, 0, -1);

        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    /** Nameplates take formatting codes, not RGB: white, gold and red match the HUD colours. */
    private static String colorCode(int fuse) {
        int color = TimerFormat.color(fuse);
        if (color == TimerFormat.COLOR_DANGER) return TextFormatting.RED.toString();
        if (color == TimerFormat.COLOR_WARNING) return TextFormatting.GOLD.toString();
        return TextFormatting.WHITE.toString();
    }
}
