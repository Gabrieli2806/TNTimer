package com.g2806.tntimer.mixin;

import com.g2806.tntimer.TNTWorldRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Shared world hook: gives fused TNT a vanilla nametag showing its countdown. */
@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {

    /** Full brightness, so the timer is readable in the dark like a glowing nametag. */
    private static final int TNTIMER$FULL_BRIGHT = 0xF000F0;

    @Shadow
    protected abstract void renderNameTag(T entity, Component text, PoseStack poseStack,
                                          MultiBufferSource buffer, int packedLight);

    // RETURN, not TAIL: render() returns early when the entity has no name to show.
    @Inject(method = "render", at = @At("RETURN"))
    private void tntimer$renderTimer(T entity, float yaw, float partialTick, PoseStack poseStack,
                                     MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        Component label = TNTWorldRenderer.labelFor(entity);
        if (label != null) {
            renderNameTag(entity, label, poseStack, buffer, TNTIMER$FULL_BRIGHT);
        }
    }
}
