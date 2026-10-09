package com.g2806.tntimer.mixin;

import com.g2806.tntimer.TNTimerHudRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Shared HUD hook: draws the flat overlay after the vanilla HUD. */
@Mixin(Gui.class)
public class HudMixin {

    // RETURN, not TAIL: Forge returns early from render() after drawing its own layered HUD.
    @Inject(method = "render", at = @At("RETURN"))
    private void tntimer$renderCountdown(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        TNTimerHudRenderer.render(graphics);
    }
}
