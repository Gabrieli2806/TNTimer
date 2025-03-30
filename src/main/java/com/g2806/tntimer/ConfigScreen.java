package com.g2806.tntimer;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

public class ConfigScreen extends Screen {
    private final Screen parent; // Pantalla padre (para Mod Menu)

    protected ConfigScreen(Screen parent) {
        super(Text.translatable("tntimer.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int buttonWidth = 200;
        int buttonHeight = 20;
        int centerX = this.width / 2 - buttonWidth / 2;
        int centerY = this.height / 2 - 100; // Ajustado para 7 botones

        // Botón para activar/desactivar el contador
        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("tntimer.config.enabled", TNTCountdown.config.isEnabled() ? Text.translatable("tntimer.config.on") : Text.translatable("tntimer.config.off")),
                        button -> {
                            TNTCountdown.config.setEnabled(!TNTCountdown.config.isEnabled());
                            TNTCountdown.config.save();
                            button.setMessage(Text.translatable("tntimer.config.enabled", TNTCountdown.config.isEnabled() ? Text.translatable("tntimer.config.on") : Text.translatable("tntimer.config.off")));
                        })
                .dimensions(centerX, centerY, buttonWidth, buttonHeight)
                .build());

        // Botón cíclico para la posición
        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("tntimer.config.position", TNTCountdown.config.getCornerPosition().getDisplayName()),
                        button -> {
                            TNTCountdown.config.setCornerPosition(TNTCountdown.config.getCornerPosition().next());
                            TNTCountdown.config.save();
                            button.setMessage(Text.translatable("tntimer.config.position", TNTCountdown.config.getCornerPosition().getDisplayName()));
                        })
                .dimensions(centerX, centerY + 30, buttonWidth, buttonHeight)
                .build());

        // Botón cíclico para la cantidad máxima de TNT
        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("tntimer.config.max_tnt", TNTCountdown.config.getMaxTntDisplay()),
                        button -> {
                            TNTCountdown.config.setMaxTntDisplay((TNTCountdown.config.getMaxTntDisplay() % 10) + 1);
                            TNTCountdown.config.save();
                            button.setMessage(Text.translatable("tntimer.config.max_tnt", TNTCountdown.config.getMaxTntDisplay()));
                        })
                .dimensions(centerX, centerY + 60, buttonWidth, buttonHeight)
                .build());

        // Botón cíclico para el tamaño del texto
        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("tntimer.config.text_size", TNTCountdown.config.getTextSize().getDisplayName()),
                        button -> {
                            TNTCountdown.config.setTextSize(TNTCountdown.config.getTextSize().next());
                            TNTCountdown.config.save();
                            button.setMessage(Text.translatable("tntimer.config.text_size", TNTCountdown.config.getTextSize().getDisplayName()));
                        })
                .dimensions(centerX, centerY + 90, buttonWidth, buttonHeight)
                .build());

        // Botón para alternar entre texto completo y solo segundos
        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("tntimer.config.display_mode", TNTCountdown.config.isShowOnlySeconds() ? Text.translatable("tntimer.config.only_seconds") : Text.translatable("tntimer.config.full_text")),
                        button -> {
                            TNTCountdown.config.setShowOnlySeconds(!TNTCountdown.config.isShowOnlySeconds());
                            TNTCountdown.config.save();
                            button.setMessage(Text.translatable("tntimer.config.display_mode", TNTCountdown.config.isShowOnlySeconds() ? Text.translatable("tntimer.config.only_seconds") : Text.translatable("tntimer.config.full_text")));
                        })
                .dimensions(centerX, centerY + 120, buttonWidth, buttonHeight)
                .build());

        // Botón para activar/desactivar el fondo semitransparente
        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("tntimer.config.background", TNTCountdown.config.isShowBackground() ? Text.translatable("tntimer.config.on") : Text.translatable("tntimer.config.off")),
                        button -> {
                            TNTCountdown.config.setShowBackground(!TNTCountdown.config.isShowBackground());
                            TNTCountdown.config.save();
                            button.setMessage(Text.translatable("tntimer.config.background", TNTCountdown.config.isShowBackground() ? Text.translatable("tntimer.config.on") : Text.translatable("tntimer.config.off")));
                        })
                .dimensions(centerX, centerY + 150, buttonWidth, buttonHeight)
                .build());

        // Botón para cerrar el menú
        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("tntimer.config.done"),
                        button -> {
                            if (this.client != null) { // Verificación para evitar NullPointerException
                                this.client.setScreen(parent);
                            }
                        })
                .dimensions(centerX, centerY + 180, buttonWidth, buttonHeight)
                .build());

        // Botón de donación y botón "X" (solo si showDonateButton es true)
        if (TNTCountdown.config.isShowDonateButton()) {
            // Botón de donación (esquina superior derecha con más margen)
            ButtonWidget donateButton = ButtonWidget.builder(
                            Text.translatable("tntimer.donate"),
                            button -> Util.getOperatingSystem().open("https://ko-fi.com/gabrieli2806"))
                    .dimensions(this.width - 90, 20, 60, 20) // Más margen: 20 píxeles desde arriba, 90 píxeles desde la derecha
                    .build();
            this.addDrawableChild(donateButton);

            // Botón "X" para ocultar el botón de donación (a la derecha del botón "Donar")
            this.addDrawableChild(ButtonWidget.builder(
                            Text.literal("X"),
                            button -> {
                                TNTCountdown.config.setShowDonateButton(false);
                                TNTCountdown.config.save();
                                // Remover ambos botones de la pantalla actual
                                this.remove(donateButton);
                                this.remove(button);
                            })
                    .dimensions(this.width - 25, 20, 20, 20) // Más margen: 20 píxeles desde arriba, 25 píxeles desde la derecha
                    .build());
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        // Añadir "Made By Gabrieli2806" en la esquina inferior derecha
        Text credits = Text.translatable("tntimer.credits");
        int textWidth = this.textRenderer.getWidth(credits);
        int x = this.width - textWidth - 5; // 5 píxeles de margen desde el borde derecho
        int y = this.height - this.textRenderer.fontHeight - 5; // 5 píxeles de margen desde el borde inferior
        context.drawText(this.textRenderer, credits, x, y, 0xFFFFFF, false);
    }
}