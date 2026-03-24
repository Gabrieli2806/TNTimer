package com.g2806.tntimer;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.vertex.PoseStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Renders TNT countdown timers as 3D nametag-style text above active TNT entities.
 * Uses MC 26.1's deferred rendering system via {@link OrderedSubmitNodeCollector#submitNameTag}.
 * Labels are submitted during COLLECT_SUBMITS and drawn by the world renderer alongside
 * vanilla entity nametags.
 */
public class TNTWorldRenderer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TNTWorldRenderer.class);
    private static final double VERTICAL_OFFSET = 0.5;

    public static void register() {
        LevelRenderEvents.COLLECT_SUBMITS.register(TNTWorldRenderer::render);
        LOGGER.info("TNTWorldRenderer: Registered COLLECT_SUBMITS level render event");
    }

    private static int debugCounter = 0;

    private static void render(LevelRenderContext context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null || mc.player == null) return;

        TNTimerConfig config = TNTimerConfig.getInstance();
        if (!config.enabled || config.displayMode != TNTimerConfig.DisplayMode.WORLD) return;

        List<PrimedTnt> tntEntities = new ArrayList<>();
        for (var entity : mc.level.entitiesForRendering()) {
            if (entity instanceof PrimedTnt tnt) {
                tntEntities.add(tnt);
            }
        }

        if (debugCounter++ % 100 == 0) {
            LOGGER.info("TNTWorldRenderer: event fired, TNT count={}, config.enabled={}, displayMode={}",
                    tntEntities.size(), config.enabled, config.displayMode);
        }

        if (tntEntities.isEmpty()) return;

        CameraRenderState cameraState = context.levelState().cameraRenderState;
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().position();

        SubmitNodeCollector collector = context.submitNodeCollector();
        PoseStack poseStack = context.poseStack();

        tntEntities.sort(Comparator.<PrimedTnt>comparingDouble(
                tnt -> tnt.distanceToSqr(cameraPos)).reversed());

        int displayCount = Math.min(tntEntities.size(), config.maxTntDisplay);

        for (int i = 0; i < displayCount; i++) {
            PrimedTnt tnt = tntEntities.get(i);
            submitLabel(tnt, poseStack, collector, cameraState, cameraPos, config);
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

        double squaredDist = x * x + y * y + z * z;

        Vec3 nameTagAttachment = new Vec3(0.0, tnt.getBbHeight() + VERTICAL_OFFSET, 0.0);

        poseStack.pushPose();
        poseStack.translate(x, y, z);

        // Parameters match vanilla EntityRenderer.submitNameDisplay:
        // poseStack, nameTagAttachment, yOffset (0), text, visible (true), 
        // lightCoords (full bright), distanceToCameraSq, cameraState
        collector.submitNameTag(poseStack, nameTagAttachment, 0, labelText, true,
                LightCoordsUtil.FULL_BRIGHT, squaredDist, cameraState);

        poseStack.popPose();
    }
}
