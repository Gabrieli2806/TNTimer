package com.g2806.tntimer.mixin;

import com.g2806.tntimer.TNTWorldRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Shared world hook: draws the 3D labels into the same buffers as vanilla entity nametags. */
@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(method = "renderEntity", at = @At("TAIL"))
    private void tntimer$renderLabels(Entity entity, double cameraX, double cameraY, double cameraZ,
                                      float partialTick, PoseStack poseStack, MultiBufferSource bufferSource,
                                      CallbackInfo ci) {
        TNTWorldRenderer.onEntityRendered(entity, partialTick, poseStack, bufferSource);
    }
}
