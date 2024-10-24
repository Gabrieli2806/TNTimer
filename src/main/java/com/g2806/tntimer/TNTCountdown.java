package com.g2806.tntimer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.TntEntity;
import net.minecraft.text.Text;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TNTCountdown implements ClientModInitializer {
    private final List<TntEntity> tntEntities = new CopyOnWriteArrayList<>();

    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> render(drawContext));
    }

    private void render(DrawContext drawContext) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null) {
            tntEntities.clear();
            client.world.getEntities().forEach(entity -> {
                if (entity instanceof TntEntity tnt) {
                    tntEntities.add(tnt);
                }
            });

            for (int i = 0; i < tntEntities.size(); i++) {
                TntEntity tnt = tntEntities.get(i);
                int fuse = tnt.getFuse();
                String timeLeft = String.format("TNT explosion in: %.1f seconds", fuse / 20.0);
                int color = 0xFFFFFF;
                if (fuse < 20) {
                    color = 0xFF0000;
                } else if (fuse < 40) {
                    color = 0xFF8000;
                }
                int screenWidth = client.getWindow().getScaledWidth();
                int x = screenWidth / 2 - client.textRenderer.getWidth(timeLeft) / 2;
                int y = 10 + i * (client.textRenderer.fontHeight + 5); // Posiciones diferentes para cada TNT
                drawContext.drawText(client.textRenderer, Text.of(timeLeft), x, y, color, false);
            }
        }
    }
}
