package com.g2806.tntimer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumChatFormatting;
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
        if (mc.theWorld == null || mc.gameSettings.hideGUI) return;

        TNTimerConfig config = TNTimerConfig.getInstance();
        if (!config.enabled || config.displayMode != TNTimerConfig.DisplayMode.WORLD) return;

        RenderManager rm = RenderManager.instance;
        if (mc.renderViewEntity == null) return;
        final double camX = rm.viewerPosX;
        final double camY = rm.viewerPosY;
        final double camZ = rm.viewerPosZ;

        List<Entity> entities = FusedEntities.collect(mc.theWorld);
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

            drawLabel(mc.fontRendererObj, label, x, y + entity.height + (float) VERTICAL_OFFSET, z,
                    rm.playerViewY, rm.playerViewX, thirdPersonFrontal);
        }
    }

    private static void drawLabel(FontRenderer font, String text, float x, float y, float z,
                                  float viewerYaw, float viewerPitch, boolean thirdPersonFrontal) {
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, z);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-viewerYaw, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef((thirdPersonFrontal ? -1 : 1) * viewerPitch, 1.0F, 0.0F, 0.0F);
        GL11.glScalef(-0.025F, -0.025F, 0.025F);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);

        int halfWidth = font.getStringWidth(text) / 2;
        Tessellator tessellator = Tessellator.instance;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA_F(0.0F, 0.0F, 0.0F, 0.25F);
        tessellator.addVertex(-halfWidth - 1, -1, 0);
        tessellator.addVertex(-halfWidth - 1, 8, 0);
        tessellator.addVertex(halfWidth + 1, 8, 0);
        tessellator.addVertex(halfWidth + 1, -1, 0);
        tessellator.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        // Faint pass visible through blocks, then the solid pass on top, like a vanilla nametag.
        font.drawString(text, -halfWidth, 0, 0x20FFFFFF);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        font.drawString(text, -halfWidth, 0, -1);

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPopMatrix();
    }

    /** Nameplates take formatting codes, not RGB: white, gold and red match the HUD colours. */
    private static String colorCode(int fuse) {
        int color = TimerFormat.color(fuse);
        if (color == TimerFormat.COLOR_DANGER) return EnumChatFormatting.RED.toString();
        if (color == TimerFormat.COLOR_WARNING) return EnumChatFormatting.GOLD.toString();
        return EnumChatFormatting.WHITE.toString();
    }
}
