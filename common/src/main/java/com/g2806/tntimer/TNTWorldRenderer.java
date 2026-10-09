package com.g2806.tntimer;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Decides which TNT gets a 3D countdown label and what it says. The label itself is drawn by
 * vanilla's own EntityRenderer.renderNameTag from the shared EntityRenderer mixin, so it looks
 * and behaves exactly like a nametag (billboarded, visible through blocks, 64 block range).
 */
public final class TNTWorldRenderer {

    /** Vanilla only draws nametags within 64 blocks; match it. */
    private static final double MAX_DISTANCE_SQR = 64.0 * 64.0;

    /** Entities allowed a label this frame (closest first, up to the configured maximum). */
    private static Set<Entity> labelled = Collections.emptySet();
    private static long labelledFrameKey = Long.MIN_VALUE;

    private TNTWorldRenderer() {
    }

    /** The label for {@code entity} this frame, or null if it shouldn't get one. */
    public static Component labelFor(Entity entity) {
        int fuse = FusedEntities.fuseOf(entity);
        if (fuse == FusedEntities.NO_FUSE) return null;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.options.hideGui) return null;

        TNTimerConfig config = TNTimerConfig.getInstance();
        if (!config.enabled || config.displayMode != TNTimerConfig.DisplayMode.WORLD) return null;

        if (!labelledThisFrame(mc, config).contains(entity)) return null;

        return new TextComponent(TimerFormat.format(fuse, config.showOnlySeconds))
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(TimerFormat.color(fuse) & 0xFFFFFF)));
    }

    /** Recomputed once per frame: closest fused entities, so the max-timers limit keeps the ones that matter. */
    private static Set<Entity> labelledThisFrame(Minecraft mc, TNTimerConfig config) {
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
        long frameKey = mc.level.getGameTime() * 31 + Double.hashCode(cameraPos.x + cameraPos.y * 3 + cameraPos.z * 7);
        if (frameKey == labelledFrameKey) return labelled;
        labelledFrameKey = frameKey;

        List<Entity> entities = FusedEntities.collect(mc.level);
        entities.removeIf(entity -> entity.distanceToSqr(cameraPos) > MAX_DISTANCE_SQR);
        entities.sort(Comparator.comparingDouble(entity -> entity.distanceToSqr(cameraPos)));
        labelled = new HashSet<>(entities.subList(0, Math.min(entities.size(), config.maxTntDisplay)));
        return labelled;
    }
}
