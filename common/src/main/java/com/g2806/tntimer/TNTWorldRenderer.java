package com.g2806.tntimer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.Comparator;
import java.util.List;

/**
 * Renders TNT countdown timers as 3D nametag-style text above active TNT entities.
 * Called from the shared LevelRenderer mixin while vanilla entities (and their nametags)
 * are drawn, and draws exactly like vanilla's EntityRenderer.renderNameTag.
 */
public final class TNTWorldRenderer {

    private static final double VERTICAL_OFFSET = 0.5;
    /** Vanilla only draws nametags within 64 blocks; match it. */
    private static final double MAX_DISTANCE_SQR = 64.0 * 64.0;
    private static final float TEXT_SCALE = 0.025F;
    /** Colour of the see-through pass vanilla uses for nametags (white, half transparent). */
    private static final int SEE_THROUGH_COLOR = 0x80FFFFFF;

    /** Identifies the frame we last drew labels in, so they're drawn once per frame. */
    private static long lastFrameKey = Long.MIN_VALUE;

    private TNTWorldRenderer() {
    }

    /**
     * Called after vanilla renders each entity. 1.21/1.21.1 have no "after all entities" method
     * to hook, so the first fused entity rendered in a frame draws every label for that frame.
     */
    public static void onEntityRendered(Entity entity, float partialTick, PoseStack poseStack,
                                        MultiBufferSource bufferSource) {
        if (FusedEntities.fuseOf(entity) == FusedEntities.NO_FUSE) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        long frameKey = mc.level.getGameTime() * 31 + Float.floatToIntBits(partialTick);
        if (frameKey == lastFrameKey) return;
        lastFrameKey = frameKey;

        render(poseStack, bufferSource, mc.gameRenderer.getMainCamera(), partialTick);
    }

    private static void render(PoseStack poseStack, MultiBufferSource bufferSource, Camera camera, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.options.hideGui) return;

        TNTimerConfig config = TNTimerConfig.getInstance();
        if (!config.enabled || config.displayMode != TNTimerConfig.DisplayMode.WORLD) return;

        Vec3 cameraPos = camera.getPosition();
        List<Entity> entities = FusedEntities.collect(mc.level);
        entities.removeIf(entity -> entity.distanceToSqr(cameraPos) > MAX_DISTANCE_SQR);
        if (entities.isEmpty()) return;

        // Closest first, so the max-timers limit keeps the labels that matter most.
        entities.sort(Comparator.comparingDouble(entity -> entity.distanceToSqr(cameraPos)));

        int backgroundColor = (int) (mc.options.getBackgroundOpacity(0.25F) * 255.0F) << 24;
        int displayCount = Math.min(entities.size(), config.maxTntDisplay);
        for (int i = 0; i < displayCount; i++) {
            renderLabel(entities.get(i), partialTick, poseStack, bufferSource, camera, mc.font,
                    backgroundColor, config);
        }
    }

    private static void renderLabel(Entity entity, float partialTick, PoseStack poseStack,
                                    MultiBufferSource bufferSource, Camera camera, Font font,
                                    int backgroundColor, TNTimerConfig config) {
        int fuse = FusedEntities.fuseOf(entity);
        Component label = Component.literal(TimerFormat.format(fuse, config.showOnlySeconds))
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(TimerFormat.color(fuse) & 0xFFFFFF)));

        // Interpolated position, so labels on moving TNT don't jitter.
        Vec3 pos = entity.getPosition(partialTick).subtract(camera.getPosition());

        poseStack.pushPose();
        poseStack.translate(pos.x, pos.y + entity.getBbHeight() + VERTICAL_OFFSET, pos.z);
        // Rotate the matrix directly: PoseStack.mulPose's parameter type changed in 1.21.5
        // (Quaternionf -> Quaternionfc), which would break one jar spanning 1.21.2-1.21.5.
        poseStack.last().pose().rotate(camera.rotation());
        poseStack.scale(TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);

        Matrix4f matrix = poseStack.last().pose();
        float x = -font.width(label) / 2.0F;
        // Same two passes as a vanilla nametag: faint see-through text with background,
        // then the solid text on top.
        font.drawInBatch(label, x, 0, SEE_THROUGH_COLOR, false, matrix, bufferSource,
                true, backgroundColor, LightTexture.FULL_BRIGHT);
        font.drawInBatch(label, x, 0, -1, false, matrix, bufferSource,
                false, 0, LightTexture.FULL_BRIGHT);

        poseStack.popPose();
    }
}
