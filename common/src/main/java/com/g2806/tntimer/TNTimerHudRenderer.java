package com.g2806.tntimer;

import net.minecraft.util.Mth;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.world.entity.Entity;

import java.util.Arrays;
import java.util.List;

/**
 * Draws the TNT countdown as a flat HUD overlay. Called from the shared Hud mixin,
 * so the same code path runs on every loader.
 */
public final class TNTimerHudRenderer {

    private static final int PADDING = 10;
    private static final int LINE_SPACING = 5;
    private static final int BACKGROUND_PADDING = 2;
    private static final int BACKGROUND_COLOR = 0x80000000;
    /** Keeps BOTTOM_CENTER timers above the hotbar, hearts and XP bar. */
    private static final int HOTBAR_CLEARANCE = 60;
    /** Gap between the crosshair and the first UNDER_CURSOR timer. */
    private static final int CURSOR_OFFSET = 15;

    private TNTimerHudRenderer() {
    }

    public static void render() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.options.hideGui) return;

        TNTimerConfig config = TNTimerConfig.getInstance();
        if (!config.enabled || config.displayMode != TNTimerConfig.DisplayMode.HUD) return;

        List<Entity> entities = FusedEntities.collect(mc.level);
        if (entities.isEmpty()) return;

        // Soonest explosion first, so the most urgent timer is always on top.
        int[] fuses = entities.stream().mapToInt(FusedEntities::fuseOf).toArray();
        Arrays.sort(fuses);

        drawTimers(mc.font, fuses, mc.getWindow().getGuiScaledWidth(),
                mc.getWindow().getGuiScaledHeight(), config);
    }

    private static void drawTimers(Font font, int[] fuses,
                                   int screenWidth, int screenHeight, TNTimerConfig config) {
        float scale = config.hudScale;
        int scaledWidth = Math.round(screenWidth / scale);
        int scaledHeight = Math.round(screenHeight / scale);
        int textHeight = font.lineHeight;
        int displayCount = Math.min(fuses.length, config.maxTntDisplay);

        RenderSystem.pushMatrix();
        RenderSystem.scalef(scale, scale, 1.0F);

        for (int i = 0; i < displayCount; i++) {
            int fuse = fuses[i];
            String text = TimerFormat.format(fuse, config.showOnlySeconds);
            int textWidth = font.width(text);

            int x = horizontalPosition(config.position, scaledWidth, textWidth);
            int y = verticalPosition(config.position, i, scaledHeight, textHeight);

            if (config.showBackground) {
                GuiComponent.fill(x - BACKGROUND_PADDING, y - BACKGROUND_PADDING,
                        x + textWidth + BACKGROUND_PADDING, y + textHeight + BACKGROUND_PADDING,
                        BACKGROUND_COLOR);
            }

            font.drawShadow(text, x, y, TimerFormat.color(fuse));
        }

        RenderSystem.popMatrix();
    }

    private static int horizontalPosition(TNTimerConfig.Position position, int screenWidth, int textWidth) {
        switch (position) {
            case TOP_LEFT:
            case BOTTOM_LEFT:
                return PADDING;
            case TOP_RIGHT:
            case BOTTOM_RIGHT:
                return screenWidth - textWidth - PADDING;
            default: // TOP_CENTER, BOTTOM_CENTER, UNDER_CURSOR
                return (screenWidth - textWidth) / 2;
        }
    }

    private static int verticalPosition(TNTimerConfig.Position position, int index,
                                        int screenHeight, int textHeight) {
        int lineStep = textHeight + LINE_SPACING;
        switch (position) {
            case TOP_LEFT:
            case TOP_RIGHT:
            case TOP_CENTER:
                return PADDING + index * lineStep;
            case BOTTOM_LEFT:
            case BOTTOM_RIGHT:
                return screenHeight - textHeight - PADDING - index * lineStep;
            case BOTTOM_CENTER:
                return screenHeight - HOTBAR_CLEARANCE - index * lineStep;
            default: // UNDER_CURSOR
                return Mth.clamp(screenHeight / 2 + CURSOR_OFFSET + index * lineStep,
                        5, screenHeight - textHeight - 5);
        }
    }
}
