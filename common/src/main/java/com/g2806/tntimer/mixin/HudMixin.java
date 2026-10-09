package com.g2806.tntimer.mixin;

import com.g2806.tntimer.TNTimerHudRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Gui;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Shared HUD hook: draws the flat overlay after the vanilla HUD. */
@Mixin(Gui.class)
public class HudMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void tntimer$renderCountdown(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        // Forge/NeoForge swap in their own Gui subclass and draw the HUD from an event instead.
        if (((Object) this).getClass() != Gui.class) return;
        TNTimerHudRenderer.render(poseStack);
    }
}
