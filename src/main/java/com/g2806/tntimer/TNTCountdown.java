package com.g2806.tntimer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.PrimedTnt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class TNTCountdown implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(TNTCountdown.class);
    private static KeyMapping configKey;

    @Override
    public void onInitializeClient() {
        // Initialize configuration
        TNTimerConfig.getInstance();
        LOGGER.info("TNTCountdown: Configuration loaded!");

        // Register keybinding for configuration - Temporarily disabled due to Category constructor changes
        /*
        configKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                ResourceLocation.fromNamespaceAndPath("tntimer", "config").toString(),
                InputUtil.Type.KEYSYM,
                InputUtil.GLFW_KEY_K,
                "category.tntimer"
        ));
        */

        // Handle config key press - Temporarily disabled
        /*
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (configKey.wasPressed()) {
                try {
                    client.setScreen(TNTimerClothConfigScreen.create(client.currentScreen));
                } catch (Exception e) {
                    LOGGER.error("Failed to open config screen: {}", e.getMessage());
                    if (client.player != null) {
                        client.player.sendMessage(Component.literal("Config failed: " + e.getMessage()), false);
                    }
                }
            }
        });
        */

        // Register HUD element - building on the working debug version
        HudElementRegistry.addLast(ResourceLocation.fromNamespaceAndPath("tntimer", "countdown"), (context, tickCounter) -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.level == null) return;

            // Get configuration
            TNTimerConfig config = TNTimerConfig.getInstance();
            boolean shouldHUDRender = config.enabled && (config.displayMode == TNTimerConfig.DisplayMode.BOTH || config.displayMode == TNTimerConfig.DisplayMode.HUD_ONLY);
            if (!shouldHUDRender) return;

            // Collect TNT entities
            List<PrimedTnt> tntEntities = new ArrayList<>();
            for (var entity : mc.level.entitiesForRendering()) {
                if (entity instanceof PrimedTnt tnt) {
                    tntEntities.add(tnt);
                }
            }

            if (tntEntities.isEmpty()) return;

            // Get screen dimensions
            int screenWidth = mc.getWindow().getGuiScaledWidth();
            int screenHeight = mc.getWindow().getGuiScaledHeight();
            int padding = 10;

            // Limit the number of TNT entities to display
            int displayCount = Math.min(tntEntities.size(), config.maxTntDisplay);

            for (int i = 0; i < displayCount; i++) {
                PrimedTnt tnt = tntEntities.get(i);
                int fuse = tnt.getFuse();
                double seconds = fuse / 20.0;
                String formattedSeconds = String.format("%.1f", seconds).replace(',', '.');

                // Always show only seconds
                String timeLeft = formattedSeconds + "s";

                // Color based on time remaining
                int color = 0xFFFFFFFF; // Default white
                if (fuse < 20) {
                    color = 0xFFFF0000; // Red for less than 1 second
                } else if (fuse < 40) {
                    color = 0xFFFF8000; // Orange for less than 2 seconds
                }

                // Calculate text dimensions
                int textWidth = mc.font.width(timeLeft);
                int textHeight = mc.font.lineHeight;

                // Calculate position based on configuration
                int x, y;
                int bottomY = screenHeight - (textHeight + padding) - i * (textHeight + 5);

                switch (config.position) {
                    case TOP_LEFT:
                        x = padding;
                        y = padding + i * (textHeight + 5);
                        break;
                    case TOP_RIGHT:
                        x = screenWidth - textWidth - padding;
                        y = padding + i * (textHeight + 5);
                        break;
                    case BOTTOM_LEFT:
                        x = padding;
                        y = bottomY;
                        break;
                    case BOTTOM_RIGHT:
                        x = screenWidth - textWidth - padding;
                        y = bottomY;
                        break;
                    case TOP_CENTER:
                        x = screenWidth / 2 - textWidth / 2;
                        y = padding + i * (textHeight + 5);
                        break;
                    case BOTTOM_CENTER:
                        x = screenWidth / 2 - textWidth / 2;
                        y = screenHeight - 60 - i * (textHeight + 5);
                        break;
                    case UNDER_CURSOR:
                        x = screenWidth / 2 - textWidth / 2;
                        y = screenHeight / 2 + 15 + i * (textHeight + 5);
                        y = Math.min(y, screenHeight - textHeight - 5);
                        y = Math.max(y, 5);
                        break;
                    default:
                        x = padding;
                        y = padding + i * (textHeight + 5);
                        break;
                }

                // Draw background if enabled
                if (config.showBackground) {
                    int backgroundPadding = 2;
                    int backgroundX = x - backgroundPadding;
                    int backgroundY = y - backgroundPadding;
                    int backgroundWidth = textWidth + backgroundPadding * 2;
                    int backgroundHeight = textHeight + backgroundPadding * 2;
                    int backgroundColor = 0x80000000; // Semi-transparent black

                    context.fill(backgroundX, backgroundY,
                            backgroundX + backgroundWidth,
                            backgroundY + backgroundHeight,
                            backgroundColor);
                }

                // Draw the countdown text - using the working method
                context.drawString(mc.font, timeLeft, x, y, color, true);
            }
        });

        LOGGER.info("TNTCountdown: Loaded successfully!");
        System.out.println("TNTCountdown: Press K to open configuration");
    }
}