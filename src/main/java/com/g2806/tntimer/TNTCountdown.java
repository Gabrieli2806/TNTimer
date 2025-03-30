package com.g2806.tntimer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Mod(modid = "tntimer", name = "TNT Countdown", version = "1.7-1.12.2", clientSideOnly = true)
public class TNTCountdown {
    private final List<EntityTNTPrimed> tntEntities = new ArrayList<>();
    public static Config config;
    private static KeyBinding configKey;

    // Enum for positions
    public enum Position {
        TOP_LEFT("Top Left"),
        TOP_RIGHT("Top Right"),
        BOTTOM_LEFT("Bottom Left"),
        BOTTOM_RIGHT("Bottom Right"),
        TOP_CENTER("Top Center"),
        BOTTOM_CENTER("Bottom Center"),
        UNDER_CURSOR("Under Cursor");

        private final String displayName;

        Position(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Position next() {
            Position[] values = values();
            return values[(ordinal() + 1) % values.length];
        }
    }

    // Enum for text sizes
    public enum TextSize {
        SMALL("Small", 0.5f),
        MEDIUM("Medium", 1.0f),
        LARGE("Large", 1.5f);

        private final String displayName;
        private final float scale;

        TextSize(String displayName, float scale) {
            this.displayName = displayName;
            this.scale = scale;
        }

        public String getDisplayName() {
            return displayName;
        }

        public float getScale() {
            return scale;
        }

        public TextSize next() {
            TextSize[] values = values();
            return values[(ordinal() + 1) % values.length];
        }
    }

    // Configuration class
    public static class Config {
        private Position cornerPosition;
        private int maxTntDisplay;
        private boolean enabled;
        private TextSize textSize;
        private boolean showOnlySeconds;
        private boolean showBackground;
        private boolean showDonateButton;
        private final Configuration configFile;

        public Config(File configFile) {
            this.configFile = new Configuration(configFile);
            load();
        }

        public void load() {
            configFile.load();
            try {
                cornerPosition = Position.valueOf(configFile.getString("cornerPosition", "General", Position.TOP_LEFT.name(), "Position of the TNT timer on the screen"));
            } catch (IllegalArgumentException e) {
                cornerPosition = Position.TOP_LEFT;
            }
            maxTntDisplay = configFile.getInt("maxTntDisplay", "General", 5, 1, 10, "Maximum number of TNT timers to display");
            enabled = configFile.getBoolean("enabled", "General", true, "Whether the TNT timer is enabled");
            try {
                textSize = TextSize.valueOf(configFile.getString("textSize", "General", TextSize.MEDIUM.name(), "Size of the timer text"));
            } catch (IllegalArgumentException e) {
                textSize = TextSize.MEDIUM;
            }
            showOnlySeconds = configFile.getBoolean("showOnlySeconds", "General", false, "Show only seconds (e.g., '2.5s') instead of full text");
            showBackground = configFile.getBoolean("showBackground", "General", false, "Show a semi-transparent background behind the timer");
            showDonateButton = configFile.getBoolean("showDonateButton", "General", true, "Show the donate button in the config screen");
        }

        public void save() {
            configFile.get("General", "cornerPosition", Position.TOP_LEFT.name()).set(cornerPosition.name());
            configFile.get("General", "maxTntDisplay", 5).set(maxTntDisplay);
            configFile.get("General", "enabled", true).set(enabled);
            configFile.get("General", "textSize", TextSize.MEDIUM.name()).set(textSize.name());
            configFile.get("General", "showOnlySeconds", false).set(showOnlySeconds);
            configFile.get("General", "showBackground", false).set(showBackground);
            configFile.get("General", "showDonateButton", true).set(showDonateButton);
            configFile.save();
        }

        public Position getCornerPosition() { return cornerPosition; }
        public void setCornerPosition(Position cornerPosition) { this.cornerPosition = cornerPosition; }
        public int getMaxTntDisplay() { return maxTntDisplay; }
        public void setMaxTntDisplay(int maxTntDisplay) { this.maxTntDisplay = maxTntDisplay; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public TextSize getTextSize() { return textSize; }
        public void setTextSize(TextSize textSize) { this.textSize = textSize; }
        public boolean isShowOnlySeconds() { return showOnlySeconds; }
        public void setShowOnlySeconds(boolean showOnlySeconds) { this.showOnlySeconds = showOnlySeconds; }
        public boolean isShowBackground() { return showBackground; }
        public void setShowBackground(boolean showBackground) { this.showBackground = showBackground; }
        public boolean isShowDonateButton() { return showDonateButton; }
        public void setShowDonateButton(boolean showDonateButton) { this.showDonateButton = showDonateButton; }
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        config = new Config(event.getSuggestedConfigurationFile());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        configKey = new KeyBinding("Open TNTimer Config", Keyboard.KEY_K, "TNTimer");
        ClientRegistry.registerKeyBinding(configKey);
        MinecraftForge.EVENT_BUS.register(this);
    }


    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null || !config.isEnabled()) return;
        if (mc.currentScreen instanceof ConfigScreen) return;

        if (configKey.isPressed() && mc.currentScreen == null) {
            mc.displayGuiScreen(new ConfigScreen(null));
        }

        tntEntities.clear();
        for (Entity entity : mc.world.loadedEntityList) {
            if (entity instanceof EntityTNTPrimed) {
                tntEntities.add((EntityTNTPrimed) entity);
            }
        }

        ScaledResolution scaledResolution = new ScaledResolution(mc);
        int screenWidth = scaledResolution.getScaledWidth();
        int screenHeight = scaledResolution.getScaledHeight();

        int padding = 10;
        int displayCount = Math.min(tntEntities.size(), config.getMaxTntDisplay());
        float scale = config.getTextSize().getScale();
        FontRenderer fontRenderer = mc.fontRenderer;

        for (int i = 0; i < displayCount; i++) {
            EntityTNTPrimed tnt = tntEntities.get(i);
            int fuse = tnt.getFuse();
            double seconds = fuse / 20.0;
            String formattedSeconds = String.format("%.1f", seconds).replace(',', '.');
            String timeLeft = config.isShowOnlySeconds() ? formattedSeconds + "s" : "TNT explosion in " + formattedSeconds + "s";
            int color = 0xFFFFFF; // White
            if (fuse < 20) color = 0xFF0000; // Red
            else if (fuse < 40) color = 0xFF8000; // Orange

            int textWidth = fontRenderer.getStringWidth(timeLeft);
            int textHeight = fontRenderer.FONT_HEIGHT;
            textWidth = (int) (textWidth * scale);
            textHeight = (int) (textHeight * scale);

            int x, y;
            int bottomY = screenHeight - padding - textHeight - i * (textHeight + 5);
            switch (config.getCornerPosition()) {
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
                    x = (screenWidth - textWidth) / 2;
                    y = padding + i * (textHeight + 5);
                    break;
                case BOTTOM_CENTER:
                    x = (screenWidth - textWidth) / 2;
                    y = screenHeight - 64 - i * (textHeight + 5); // Justo encima de la hotbar, apilando hacia arriba
                    break;
                case UNDER_CURSOR:
                    x = (screenWidth - textWidth) / 2;
                    y = (screenHeight / 2) + 15 + i * (textHeight + 5);
                    y = Math.min(y, screenHeight - textHeight - padding);
                    y = Math.max(y, padding);
                    break;
                default: // TOP_LEFT and any invalid value
                    x = padding;
                    y = padding + i * (textHeight + 5);
                    break;
            }

            GlStateManager.pushMatrix();
            try {
                GlStateManager.translate(x, y, 0);
                GlStateManager.scale(scale, scale, 1.0f);

                if (config.isShowBackground()) {
                    int backgroundPadding = 2;
                    int backgroundX = -backgroundPadding;
                    int backgroundY = -backgroundPadding;
                    int backgroundWidth = fontRenderer.getStringWidth(timeLeft) + backgroundPadding * 2;
                    int backgroundHeight = fontRenderer.FONT_HEIGHT + backgroundPadding * 2;
                    drawRect(backgroundX, backgroundY, backgroundX + backgroundWidth, backgroundY + backgroundHeight, 0x80000000);
                }

                fontRenderer.drawString(timeLeft, 0, 0, color);
            } finally {
                GlStateManager.popMatrix();
            }
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void drawRect(int left, int top, int right, int bottom, int color) {
        float alpha = (float) (color >> 24 & 255) / 255.0F;
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.color(red, green, blue, alpha);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION);
        buffer.pos(left, bottom, 0).endVertex();
        buffer.pos(right, bottom, 0).endVertex();
        buffer.pos(right, top, 0).endVertex();
        buffer.pos(left, top, 0).endVertex();
        tessellator.draw();
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
    }
}