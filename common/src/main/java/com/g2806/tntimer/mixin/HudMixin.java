package com.g2806.tntimer.mixin;

import com.g2806.tntimer.TNTimerHudRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Shared HUD hook: draws the flat overlay after vanilla HUD elements are extracted. */
@Mixin(Hud.class)
public class HudMixin {

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void tntimer$renderCountdown(GuiGraphicsExtractor extractor, DeltaTracker deltaTracker, CallbackInfo ci) {
        TNTimerHudRenderer.render(extractor);
    }
}
