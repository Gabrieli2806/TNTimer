package com.g2806.tntimer.mixin;

import com.g2806.tntimer.TNTWorldRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Shared world hook: submits the 3D labels alongside vanilla entity nametags. */
@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(method = "submitEntities", at = @At("TAIL"))
    private void tntimer$submitLabels(PoseStack poseStack, LevelRenderState levelState,
                                      SubmitNodeCollector collector, CallbackInfo ci) {
        TNTWorldRenderer.submit(poseStack, levelState, collector);
    }
}
