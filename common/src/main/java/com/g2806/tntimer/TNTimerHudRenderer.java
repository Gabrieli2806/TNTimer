package com.g2806.tntimer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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

    public static void render(GuiGraphicsExtractor context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.gui.hud.isHidden()) return;

        TNTimerConfig config = TNTimerConfig.getInstance();
        if (!config.enabled || config.displayMode != TNTimerConfig.DisplayMode.HUD) return;

        List<Entity> entities = FusedEntities.collect(mc.level, config.showSulfurCubes);
        if (entities.isEmpty()) return;

        // Soonest explosion first, so the most urgent timer is always on top.
        int[] fuses = entities.stream().mapToInt(FusedEntities::fuseOf).toArray();
        Arrays.sort(fuses);

        drawTimers(context, mc.font, fuses, mc.getWindow().getGuiScaledWidth(),
                mc.getWindow().getGuiScaledHeight(), config);
    }

    private static void drawTimers(GuiGraphicsExtractor context, Font font, int[] fuses,
                                   int screenWidth, int screenHeight, TNTimerConfig config) {
        float scale = config.hudScale;
        int scaledWidth = Math.round(screenWidth / scale);
        int scaledHeight = Math.round(screenHeight / scale);
        int textHeight = font.lineHeight;
        int displayCount = Math.min(fuses.length, config.maxTntDisplay);

        context.pose().pushMatrix();
        context.pose().scale(scale, scale);

        for (int i = 0; i < displayCount; i++) {
            int fuse = fuses[i];
            String text = TimerFormat.format(fuse, config.showOnlySeconds);
            int textWidth = font.width(text);

            int x = horizontalPosition(config.position, scaledWidth, textWidth);
            int y = verticalPosition(config.position, i, scaledHeight, textHeight);

            if (config.showBackground) {
                context.fill(x - BACKGROUND_PADDING, y - BACKGROUND_PADDING,
                        x + textWidth + BACKGROUND_PADDING, y + textHeight + BACKGROUND_PADDING,
                        BACKGROUND_COLOR);
            }

            context.text(font, text, x, y, TimerFormat.color(fuse));
        }

        context.pose().popMatrix();
    }

    private static int horizontalPosition(TNTimerConfig.Position position, int screenWidth, int textWidth) {
        return switch (position) {
            case TOP_LEFT, BOTTOM_LEFT -> PADDING;
            case TOP_RIGHT, BOTTOM_RIGHT -> screenWidth - textWidth - PADDING;
            case TOP_CENTER, BOTTOM_CENTER, UNDER_CURSOR -> (screenWidth - textWidth) / 2;
        };
    }

    private static int verticalPosition(TNTimerConfig.Position position, int index,
                                        int screenHeight, int textHeight) {
        int lineStep = textHeight + LINE_SPACING;
        return switch (position) {
            case TOP_LEFT, TOP_RIGHT, TOP_CENTER -> PADDING + index * lineStep;
            case BOTTOM_LEFT, BOTTOM_RIGHT -> screenHeight - textHeight - PADDING - index * lineStep;
            case BOTTOM_CENTER -> screenHeight - HOTBAR_CLEARANCE - index * lineStep;
            case UNDER_CURSOR -> Math.clamp(screenHeight / 2 + CURSOR_OFFSET + index * lineStep,
                    5, screenHeight - textHeight - 5);
        };
    }
}
