package com.g2806.tntimer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.TntEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class TNTCountdown implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(TNTCountdown.class);
    private static KeyBinding configKey;

    @Override
    public void onInitializeClient() {
        // Initialize configuration
        TNTimerConfig.getInstance();
        LOGGER.info("TNTCountdown: Configuration loaded!");

        // Register keybinding for configuration
        configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.tntimer.config",
                InputUtil.Type.KEYSYM,
                InputUtil.GLFW_KEY_K,
                "category.tntimer"
        ));

        // Handle config key press
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (configKey.wasPressed()) {
                try {
                    client.setScreen(TNTimerClothConfigScreen.create(client.currentScreen));
                } catch (Exception e) {
                    LOGGER.error("Failed to open config screen: {}", e.getMessage());
                    if (client.player != null) {
                        client.player.sendMessage(Text.literal("Config failed: " + e.getMessage()), false);
                    }
                }
            }
        });

        // Register HUD element - building on the working debug version
        HudElementRegistry.addLast(Identifier.of("tntimer", "countdown"), (context, tickCounter) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc == null || mc.world == null) return;

            // Get configuration
            TNTimerConfig config = TNTimerConfig.getInstance();
            if (!config.enabled) return;

            // Collect TNT entities
            List<TntEntity> tntEntities = new ArrayList<>();
            for (var entity : mc.world.getEntities()) {
                if (entity instanceof TntEntity tnt) {
                    tntEntities.add(tnt);
                }
            }

            if (tntEntities.isEmpty()) return;

            // Get screen dimensions
            int screenWidth = mc.getWindow().getScaledWidth();
            int screenHeight = mc.getWindow().getScaledHeight();
            int padding = 10;

            // Limit the number of TNT entities to display
            int displayCount = Math.min(tntEntities.size(), config.maxTntDisplay);

            for (int i = 0; i < displayCount; i++) {
                TntEntity tnt = tntEntities.get(i);
                int fuse = tnt.getFuse();
                double seconds = fuse / 20.0;
                String formattedSeconds = String.format("%.1f", seconds).replace(',', '.');

                String timeLeft;
                if (config.showOnlySeconds) {
                    timeLeft = formattedSeconds + "s";
                } else {
                    timeLeft = "TNT: " + formattedSeconds + "s";
                }

                // Color based on time remaining
                int color = Colors.WHITE; // Default white
                if (fuse < 20) {
                    color = 0xFFFF0000; // Red for less than 1 second
                } else if (fuse < 40) {
                    color = 0xFFFF8000; // Orange for less than 2 seconds
                }

                // Calculate text dimensions
                int textWidth = mc.textRenderer.getWidth(timeLeft);
                int textHeight = mc.textRenderer.fontHeight;

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
                context.drawTextWithShadow(mc.textRenderer, timeLeft, x, y, color);
            }
        });

        LOGGER.info("TNTCountdown: Loaded successfully!");
        System.out.println("TNTCountdown: Press K to open configuration");
    }
}