package com.g2806.tntimer;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Util;

public class ConfigScreen extends Screen {
    private final Screen parent; // Pantalla padre (para Mod Menu)

    protected ConfigScreen(Screen parent) {
        super(new LiteralText("TNT Countdown Config")); // Replaced Text.literal with new LiteralText
        this.parent = parent;
    }

    @Override
    protected void init() {
        int buttonWidth = 200;
        int buttonHeight = 20;
        int centerX = this.width / 2 - buttonWidth / 2;
        int centerY = this.height / 2 - 100; // Ajustado para 7 botones

        // Botón para activar/desactivar el contador
        this.addDrawableChild(new ButtonWidget(
                centerX, centerY, buttonWidth, buttonHeight,
                new LiteralText("Timer: " + (TNTCountdown.config.isEnabled() ? "On" : "Off")), // Replaced Text.literal
                button -> {
                    TNTCountdown.config.setEnabled(!TNTCountdown.config.isEnabled());
                    TNTCountdown.config.save();
                    button.setMessage(new LiteralText("Timer: " + (TNTCountdown.config.isEnabled() ? "On" : "Off")));
                }));

        // Botón cíclico para la posición
        this.addDrawableChild(new ButtonWidget(
                centerX, centerY + 30, buttonWidth, buttonHeight,
                new LiteralText("Position: " + TNTCountdown.config.getCornerPosition().getDisplayName().getString()), // Replaced Text.literal
                button -> {
                    TNTCountdown.config.setCornerPosition(TNTCountdown.config.getCornerPosition().next());
                    TNTCountdown.config.save();
                    button.setMessage(new LiteralText("Position: " + TNTCountdown.config.getCornerPosition().getDisplayName().getString()));
                }));

        // Botón cíclico para la cantidad máxima de TNT
        this.addDrawableChild(new ButtonWidget(
                centerX, centerY + 60, buttonWidth, buttonHeight,
                new LiteralText("Max TNT Display: " + TNTCountdown.config.getMaxTntDisplay()), // Replaced Text.literal
                button -> {
                    TNTCountdown.config.setMaxTntDisplay((TNTCountdown.config.getMaxTntDisplay() % 10) + 1);
                    TNTCountdown.config.save();
                    button.setMessage(new LiteralText("Max TNT Display: " + TNTCountdown.config.getMaxTntDisplay()));
                }));

        // Botón cíclico para el tamaño del texto
        this.addDrawableChild(new ButtonWidget(
                centerX, centerY + 90, buttonWidth, buttonHeight,
                new LiteralText("Text Size: " + TNTCountdown.config.getTextSize().getDisplayName().getString()), // Replaced Text.literal
                button -> {
                    TNTCountdown.config.setTextSize(TNTCountdown.config.getTextSize().next());
                    TNTCountdown.config.save();
                    button.setMessage(new LiteralText("Text Size: " + TNTCountdown.config.getTextSize().getDisplayName().getString()));
                }));

        // Botón para alternar entre texto completo y solo segundos
        this.addDrawableChild(new ButtonWidget(
                centerX, centerY + 120, buttonWidth, buttonHeight,
                new LiteralText("Display Mode: " + (TNTCountdown.config.isShowOnlySeconds() ? "Only Seconds" : "Full Text")), // Replaced Text.literal
                button -> {
                    TNTCountdown.config.setShowOnlySeconds(!TNTCountdown.config.isShowOnlySeconds());
                    TNTCountdown.config.save();
                    button.setMessage(new LiteralText("Display Mode: " + (TNTCountdown.config.isShowOnlySeconds() ? "Only Seconds" : "Full Text")));
                }));

        // Botón para activar/desactivar el fondo semitransparente
        this.addDrawableChild(new ButtonWidget(
                centerX, centerY + 150, buttonWidth, buttonHeight,
                new LiteralText("Background: " + (TNTCountdown.config.isShowBackground() ? "On" : "Off")), // Replaced Text.literal
                button -> {
                    TNTCountdown.config.setShowBackground(!TNTCountdown.config.isShowBackground());
                    TNTCountdown.config.save();
                    button.setMessage(new LiteralText("Background: " + (TNTCountdown.config.isShowBackground() ? "On" : "Off")));
                }));

        // Botón para cerrar el menú
        this.addDrawableChild(new ButtonWidget(
                centerX, centerY + 180, buttonWidth, buttonHeight,
                new LiteralText("Done"), // Replaced Text.literal
                button -> {
                    if (this.client != null) { // Verificación para evitar NullPointerException
                        this.client.setScreen(parent);
                    }
                }));

        // Botón de donación y botón "X" (solo si showDonateButton es true)
        if (TNTCountdown.config.isShowDonateButton()) {
            // Botón de donación (esquina superior derecha con más margen)
            ButtonWidget donateButton = new ButtonWidget(
                    this.width - 90, 20, 60, 20,
                    new LiteralText("Donate"), // Replaced Text.literal
                    button -> Util.getOperatingSystem().open("https://www.paypal.com/donate?hosted_button_id=YOUR_BUTTON_ID"));
            this.addDrawableChild(donateButton);

            // Botón "X" para ocultar el botón de donación (a la derecha del botón "Donar")
            this.addDrawableChild(new ButtonWidget(
                    this.width - 25, 20, 20, 20,
                    new LiteralText("X"), // Replaced Text.literal
                    button -> {
                        TNTCountdown.config.setShowDonateButton(false);
                        TNTCountdown.config.save();
                        // Remover ambos botones de la pantalla actual
                        this.remove(donateButton);
                        this.remove(button);
                    }));
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrixStack); // Renderizar el fondo (oscurecido)
        super.render(matrixStack, mouseX, mouseY, delta);
        // Añadir "Made By Gabrieli2806" en la esquina inferior derecha
        Text credits = new LiteralText("Made By Gabrieli2806"); // Replaced Text.literal
        int textWidth = this.textRenderer.getWidth(credits);
        int x = this.width - textWidth - 5; // 5 píxeles de margen desde el borde derecho
        int y = this.height - this.textRenderer.fontHeight - 5; // 5 píxeles de margen desde el borde inferior
        this.textRenderer.drawWithShadow(matrixStack, credits, x, y, 0xFFFFFF);
    }
}