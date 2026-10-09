package com.g2806.tntimer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

/**
 * Renders TNT countdown timers as 3D nametag-style text above active TNT entities.
 * Uses MC 26.x's deferred rendering system via the world renderer's nametag submit queue.
 * Labels are submitted from the shared LevelRenderer mixin, right after vanilla entities,
 * so the same code path runs on every loader.
 */
public final class TNTWorldRenderer {

    private static final double VERTICAL_OFFSET = 0.5;

    private TNTWorldRenderer() {
    }

    public static void submit(PoseStack poseStack, LevelRenderState levelState, SubmitNodeCollector collector) {
        Minecraft mc = Minecraft.getInstance();
        // Vanilla hides nametags with F1, so do the same for ours.
        if (mc.level == null || mc.options.hideGui) return;

        TNTimerConfig config = TNTimerConfig.getInstance();
        if (!config.enabled || config.displayMode != TNTimerConfig.DisplayMode.WORLD) return;

        CameraRenderState cameraState = levelState.cameraRenderState;
        if (cameraState == null || cameraState.pos == null) return;
        Vec3 cameraPos = cameraState.pos;
        float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);

        // No distance filter: vanilla nametag rendering already hides far-away labels.
        List<Entity> entities = FusedEntities.collect(mc.level);
        if (entities.isEmpty()) return;

        // Closest first, so the max-timers limit keeps the labels that matter most.
        entities.sort(Comparator.comparingDouble(entity -> entity.distanceToSqr(cameraPos)));

        int displayCount = Math.min(entities.size(), config.maxTntDisplay);
        for (int i = 0; i < displayCount; i++) {
            submitLabel(entities.get(i), partialTick, poseStack, collector, cameraState, cameraPos, config);
        }
    }

    private static void submitLabel(Entity entity, float partialTick, PoseStack poseStack,
                                    SubmitNodeCollector collector,
                                    CameraRenderState cameraState, Vec3 cameraPos,
                                    TNTimerConfig config) {
        int fuse = FusedEntities.fuseOf(entity);
        Component label = Component.literal(TimerFormat.format(fuse, config.showOnlySeconds))
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(TimerFormat.color(fuse) & 0xFFFFFF)));

        // Interpolated position, so labels on moving TNT or sulfur cubes don't jitter.
        Vec3 pos = entity.getPosition(partialTick).subtract(cameraPos);
        Vec3 nameTagAttachment = new Vec3(0.0, entity.getBbHeight() + VERTICAL_OFFSET, 0.0);

        poseStack.pushPose();
        poseStack.translate(pos.x, pos.y, pos.z);

        // Same arguments as vanilla EntityRenderer.submitNameDisplay: attachment point,
        // no extra y offset, always visible (not sneaking), full bright.
        collector.submitNameTag(poseStack, nameTagAttachment, 0, label, true,
                LightCoordsUtil.FULL_BRIGHT, entity.distanceToSqr(cameraPos), cameraState);

        poseStack.popPose();
    }
}
