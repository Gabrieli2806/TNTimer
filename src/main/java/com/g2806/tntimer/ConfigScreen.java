package com.g2806.tntimer;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.common.FMLLog;

public class ConfigScreen extends GuiScreen {
    private final GuiScreen parent;

    public ConfigScreen(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();

        int buttonWidth = 150;
        int buttonHeight = 20;
        int spacing = 40;
        int numButtons = 5;
        int totalHeight = (numButtons - 1) * spacing + buttonHeight;
        int startY = (this.height - totalHeight) / 2;

        int centerX = this.width / 2;
        int yOffset = startY;

        this.buttonList.add(new GuiButtonExt(1, centerX - buttonWidth / 2, yOffset, buttonWidth, buttonHeight,
                TNTCountdown.config.getCornerPosition().getDisplayName()));
        yOffset += spacing;

        this.buttonList.add(new GuiButtonExt(2, centerX - buttonWidth / 2, yOffset, buttonWidth, buttonHeight,
                String.valueOf(TNTCountdown.config.getMaxTntDisplay())));
        yOffset += spacing;

        this.buttonList.add(new GuiButtonExt(3, centerX - buttonWidth / 2, yOffset, buttonWidth, buttonHeight,
                TNTCountdown.config.getTextSize().getDisplayName()));
        yOffset += spacing;

        this.buttonList.add(new GuiButtonExt(4, centerX - buttonWidth / 2, yOffset, buttonWidth, buttonHeight,
                TNTCountdown.config.isShowOnlySeconds() ? "Only Seconds" : "Full Text"));
        yOffset += spacing;

        this.buttonList.add(new GuiButtonExt(5, centerX - buttonWidth / 2, yOffset, buttonWidth, buttonHeight,
                TNTCountdown.config.isShowBackground() ? "On" : "Off"));

        // Botón "Done" en la parte inferior
        this.buttonList.add(new GuiButtonExt(6, centerX - buttonWidth / 2, this.height - 30, buttonWidth, buttonHeight, "Done"));

        // Botones de donación en la esquina superior derecha
        if (TNTCountdown.config.isShowDonateButton()) {
            this.buttonList.add(new GuiButtonExt(7, this.width - 90, 10, 60, 20, "Donate"));
            this.buttonList.add(new GuiButtonExt(8, this.width - 25, 10, 20, 20, "X"));
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 1: // Timer Position
                TNTCountdown.config.setCornerPosition(TNTCountdown.config.getCornerPosition().next());
                TNTCountdown.config.save();
                button.displayString = TNTCountdown.config.getCornerPosition().getDisplayName();
                break;
            case 2: // Max TNT Display
                TNTCountdown.config.setMaxTntDisplay((TNTCountdown.config.getMaxTntDisplay() % 10) + 1);
                TNTCountdown.config.save();
                button.displayString = String.valueOf(TNTCountdown.config.getMaxTntDisplay());
                break;
            case 3: // Text Size
                TNTCountdown.config.setTextSize(TNTCountdown.config.getTextSize().next());
                TNTCountdown.config.save();
                button.displayString = TNTCountdown.config.getTextSize().getDisplayName();
                break;
            case 4: // Display Mode
                TNTCountdown.config.setShowOnlySeconds(!TNTCountdown.config.isShowOnlySeconds());
                TNTCountdown.config.save();
                button.displayString = TNTCountdown.config.isShowOnlySeconds() ? "Only Seconds" : "Full Text";
                break;
            case 5: // Background
                TNTCountdown.config.setShowBackground(!TNTCountdown.config.isShowBackground());
                TNTCountdown.config.save();
                button.displayString = TNTCountdown.config.isShowBackground() ? "On" : "Off";
                break;
            case 6: // Done
                TNTCountdown.config.save();
                this.mc.displayGuiScreen(parent);
                break;
            case 7: // Donate
                try {
                    java.awt.Desktop.getDesktop().browse(new java.net.URI("https://ko-fi.com/gabrieli2806"));
                } catch (Exception e) {
                    FMLLog.log.error("Failed to open donation link", e);
                }
                break;
            case 8: // Hide Donate Button
                TNTCountdown.config.setShowDonateButton(false);
                TNTCountdown.config.save();
                this.buttonList.removeIf(b -> b.id == 7 || b.id == 8);
                break;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        String title = "TNT Countdown Settings";
        int titleWidth = this.fontRenderer.getStringWidth(title);
        this.fontRenderer.drawStringWithShadow(title, (this.width - titleWidth) / 2.0f, 10, 0xFFFFFF);

        // Etiquetas ajustadas para estar más centradas entre los botones
        int centerX = this.width / 2;
        int spacing = 40;
        int numButtons = 5;
        int totalHeight = (numButtons - 1) * spacing + 20;
        int startY = (this.height - totalHeight) / 2;
        int yOffset = startY - 10; // Ajustado de -20 a -10 para centrar mejor el texto

        this.fontRenderer.drawString("Timer Position", centerX - 75, yOffset, 0xFFFFFF);
        yOffset += spacing;
        this.fontRenderer.drawString("Max TNT Display", centerX - 75, yOffset, 0xFFFFFF);
        yOffset += spacing;
        this.fontRenderer.drawString("Text Size", centerX - 75, yOffset, 0xFFFFFF);
        yOffset += spacing;
        this.fontRenderer.drawString("Display Mode", centerX - 75, yOffset, 0xFFFFFF);
        yOffset += spacing;
        this.fontRenderer.drawString("Background", centerX - 75, yOffset, 0xFFFFFF);

        String credits = "Made By Gabrieli2806";
        int textWidth = this.fontRenderer.getStringWidth(credits);
        this.fontRenderer.drawStringWithShadow(credits, this.width - textWidth - 5, this.height - this.fontRenderer.FONT_HEIGHT - 5, 0xAAAAAA);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}