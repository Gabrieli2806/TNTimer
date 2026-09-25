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
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Renders TNT countdown timers as 3D nametag-style text above active TNT entities.
 * Uses MC 26.3's deferred rendering system via the world renderer's nametag submit queue.
 * Labels are submitted from the shared LevelRenderer mixin, right after vanilla entities,
 * so the same code path runs on every loader.
 */
public final class TNTWorldRenderer {

    private static final double VERTICAL_OFFSET = 0.5;

    private TNTWorldRenderer() {
    }

    public static void submit(PoseStack poseStack, LevelRenderState levelState, SubmitNodeCollector collector) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null || mc.player == null) return;

        TNTimerConfig config = TNTimerConfig.getInstance();
        if (!config.enabled || config.displayMode != TNTimerConfig.DisplayMode.WORLD) return;

        CameraRenderState cameraState = levelState.cameraRenderState;
        if (cameraState == null || cameraState.pos == null) return;
        Vec3 cameraPos = cameraState.pos;

        List<PrimedTnt> tntEntities = new ArrayList<>();
        for (var entity : mc.level.entitiesForRendering()) {
            if (entity instanceof PrimedTnt tnt) {
                tntEntities.add(tnt);
            }
        }

        if (tntEntities.isEmpty()) return;

        tntEntities.sort(Comparator.<PrimedTnt>comparingDouble(
                tnt -> tnt.distanceToSqr(cameraPos)).reversed());

        int displayCount = Math.min(tntEntities.size(), config.maxTntDisplay);

        for (int i = 0; i < displayCount; i++) {
            submitLabel(tntEntities.get(i), poseStack, collector, cameraState, cameraPos, config);
        }
    }

    private static void submitLabel(PrimedTnt tnt, PoseStack poseStack,
                                    SubmitNodeCollector collector,
                                    CameraRenderState cameraState, Vec3 cameraPos,
                                    TNTimerConfig config) {
        int fuse = tnt.getFuse();
        double seconds = fuse / 20.0;
        String formattedSeconds = String.format("%.1f", seconds).replace(',', '.');

        String timeLeft = config.showOnlySeconds
                ? formattedSeconds + "s"
                : "TNT: " + formattedSeconds + "s";

        int color;
        if (fuse < 20) {
            color = 0xFF0000;
        } else if (fuse < 40) {
            color = 0xFF8000;
        } else {
            color = 0xFFFFFF;
        }

        Component labelText = Component.literal(timeLeft).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(color)));

        double x = tnt.getX() - cameraPos.x;
        double y = tnt.getY() - cameraPos.y;
        double z = tnt.getZ() - cameraPos.z;

        Vec3 nameTagAttachment = new Vec3(0.0, tnt.getBbHeight() + VERTICAL_OFFSET, 0.0);

        poseStack.pushPose();
        poseStack.translate(x, y, z);

        // Parameters match vanilla EntityRenderer.submitNameDisplay:
        // poseStack, nameTagAttachment, yOffset (0), text, visible (true),
        // lightCoords (full bright), cameraState
        collector.submitNameTag(poseStack, nameTagAttachment, 0, labelText, true,
                LightCoordsUtil.FULL_BRIGHT, cameraState);

        poseStack.popPose();
    }
}
