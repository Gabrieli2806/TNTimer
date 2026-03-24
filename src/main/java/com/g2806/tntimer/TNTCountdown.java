package com.g2806.tntimer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.item.PrimedTnt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class TNTCountdown implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(TNTCountdown.class);
    private static final int PADDING = 10;
    private static final int LINE_SPACING = 5;

    @Override
    public void onInitializeClient() {
        TNTimerConfig.getInstance();
        LOGGER.info("TNTCountdown: Configuration loaded!");

        // Register HUD overlay rendering
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("tntimer", "countdown"), (context, tickCounter) -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.level == null) return;

            TNTimerConfig config = TNTimerConfig.getInstance();
            if (!config.enabled || config.displayMode != TNTimerConfig.DisplayMode.HUD) return;

            List<PrimedTnt> tntEntities = collectTntEntities(mc);
            if (tntEntities.isEmpty()) return;

            int screenWidth = mc.getWindow().getGuiScaledWidth();
            int screenHeight = mc.getWindow().getGuiScaledHeight();
            int textHeight = mc.font.lineHeight;
            int displayCount = Math.min(tntEntities.size(), config.maxTntDisplay);

            for (int i = 0; i < displayCount; i++) {
                PrimedTnt tnt = tntEntities.get(i);
                int fuse = tnt.getFuse();
                String timeLeft = formatFuseTime(fuse, config.showOnlySeconds);
                int color = getFuseColor(fuse);
                int textWidth = mc.font.width(timeLeft);

                int[] pos = calculatePosition(config.position, i, screenWidth, screenHeight,
                        textWidth, textHeight, displayCount);

                if (config.showBackground) {
                    int bgPad = 2;
                    context.fill(pos[0] - bgPad, pos[1] - bgPad,
                            pos[0] + textWidth + bgPad, pos[1] + textHeight + bgPad,
                            0x80000000);
                }

                context.text(mc.font, timeLeft, pos[0], pos[1], color);
            }
        });

        // Register 3D world nametag rendering
        TNTWorldRenderer.register();

        LOGGER.info("TNTCountdown: Loaded successfully!");
    }

    private static List<PrimedTnt> collectTntEntities(Minecraft mc) {
        List<PrimedTnt> list = new ArrayList<>();
        for (var entity : mc.level.entitiesForRendering()) {
            if (entity instanceof PrimedTnt tnt) {
                list.add(tnt);
            }
        }
        return list;
    }

    static String formatFuseTime(int fuse, boolean onlySeconds) {
        double seconds = fuse / 20.0;
        String formatted = String.format("%.1f", seconds).replace(',', '.');
        return onlySeconds ? formatted + "s" : "TNT: " + formatted + "s";
    }

    static int getFuseColor(int fuse) {
        if (fuse < 20) return 0xFFFF0000;      // Red < 1s
        if (fuse < 40) return 0xFFFF8000;       // Orange < 2s
        return 0xFFFFFFFF;                       // White
    }

    private static int[] calculatePosition(TNTimerConfig.Position position, int index,
                                            int screenWidth, int screenHeight,
                                            int textWidth, int textHeight, int totalCount) {
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