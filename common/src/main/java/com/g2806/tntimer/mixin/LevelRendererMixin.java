package com.g2806.tntimer.mixin;

import com.g2806.tntimer.TNTWorldRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/** Shared world hook: draws the 3D labels into the same buffers as vanilla entity nametags. */
@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(method = "renderEntities", at = @At("TAIL"))
    private void tntimer$renderLabels(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
                                      Camera camera, DeltaTracker deltaTracker, List<Entity> entities,
                                      CallbackInfo ci) {
        TNTWorldRenderer.render(poseStack, bufferSource, camera, deltaTracker.getGameTimeDeltaPartialTick(false));
    }
}
