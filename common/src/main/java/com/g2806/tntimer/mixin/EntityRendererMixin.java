package com.g2806.tntimer.mixin;

import com.g2806.tntimer.TNTWorldRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Shared world hook: gives fused TNT a vanilla nametag showing its countdown. */
@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {

    /** Vanilla nametag range. */
    private static final int TNTIMER$MAX_DISTANCE = 64;

    @Shadow
    protected abstract void renderNameTag(T entity, String text, double x, double y, double z, int maxDistance);

    // RETURN, not TAIL: render() can return early.
    @Inject(method = "render", at = @At("RETURN"))
    private void tntimer$renderTimer(T entity, double x, double y, double z, float yaw, float partialTick,
                                     CallbackInfo ci) {
        String label = TNTWorldRenderer.labelFor(entity);
        if (label != null) {
            renderNameTag(entity, label, x, y, z, TNTIMER$MAX_DISTANCE);
        }
    }
}
