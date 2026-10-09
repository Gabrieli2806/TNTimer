package com.g2806.tntimer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.Entity;

import java.util.List;

/**
 * Draws the TNT countdown as a flat HUD overlay. Called from the shared Hud mixin,
 * so the same code path runs on every loader.
 */
public final class TNTimerHudRenderer {

    private static final int PADDING = 10;
    private static final int LINE_SPACING = 5;

    private TNTimerHudRenderer() {
    }

    public static void render(GuiGraphicsExtractor context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null) return;

        TNTimerConfig config = TNTimerConfig.getInstance();
        if (!config.enabled || config.displayMode != TNTimerConfig.DisplayMode.HUD) return;

        List<Entity> tntEntities = FusedEntities.collect(mc.level);
        if (tntEntities.isEmpty()) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int textHeight = mc.font.lineHeight;
        int displayCount = Math.min(tntEntities.size(), config.maxTntDisplay);

        for (int i = 0; i < displayCount; i++) {
            int fuse = FusedEntities.fuseOf(tntEntities.get(i));
            String timeLeft = formatFuseTime(fuse, config.showOnlySeconds);
            int color = getFuseColor(fuse);
            int textWidth = mc.font.width(timeLeft);

            int[] pos = calculatePosition(config.position, i, screenWidth, screenHeight,
                    textWidth, textHeight);

            if (config.showBackground) {
                int bgPad = 2;
                context.fill(pos[0] - bgPad, pos[1] - bgPad,
                        pos[0] + textWidth + bgPad, pos[1] + textHeight + bgPad,
                        0x80000000);
            }

            context.text(mc.font, timeLeft, pos[0], pos[1], color);
        }
    }

    static String formatFuseTime(int fuse, boolean onlySeconds) {
        double seconds = fuse / 20.0;
        String formatted = String.format("%.1f", seconds).replace(',', '.');
        return onlySeconds ? formatted + "s" : "TNT: " + formatted + "s";
    }

    static int getFuseColor(int fuse) {
        if (fuse < 20) return 0xFFFF0000;      // Red < 1s
        if (fuse < 40) return 0xFFFF8000;      // Orange < 2s
        return 0xFFFFFFFF;                     // White
    }

    private static int[] calculatePosition(TNTimerConfig.Position position, int index,
                                           int screenWidth, int screenHeight,
                                           int textWidth, int textHeight) {
        int lineStep = textHeight + LINE_SPACING;
        int x, y;

        switch (position) {
            case TOP_LEFT -> {
                x = PADDING;
                y = PADDING + index * lineStep;
            }
            case TOP_RIGHT -> {
                x = screenWidth - textWidth - PADDING;
                y = PADDING + index * lineStep;
            }
            case BOTTOM_LEFT -> {
                x = PADDING;
                y = screenHeight - (textHeight + PADDING) - index * lineStep;
            }
            case BOTTOM_RIGHT -> {
                x = screenWidth - textWidth - PADDING;
                y = screenHeight - (textHeight + PADDING) - index * lineStep;
            }
            case TOP_CENTER -> {
                x = (screenWidth - textWidth) / 2;
                y = PADDING + index * lineStep;
            }
            case BOTTOM_CENTER -> {
                x = (screenWidth - textWidth) / 2;
                y = screenHeight - 60 - index * lineStep;
            }
            case UNDER_CURSOR -> {
                x = (screenWidth - textWidth) / 2;
                y = Math.clamp(screenHeight / 2 + 15 + index * lineStep, 5, screenHeight - textHeight - 5);
            }
            default -> {
                x = PADDING;
                y = PADDING + index * lineStep;
            }
        }

        return new int[]{x, y};
    }
}
