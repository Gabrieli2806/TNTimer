package com.g2806.tntimer;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.stream.IntStream;
import java.util.stream.Collectors;

public class TNTimerConfigScreen extends Screen {

    private final Screen parent;
    private final TNTimerConfig config;

    private boolean enabled;
    private TNTimerConfig.DisplayMode displayMode;
    private TNTimerConfig.Position position;
    private int maxTntDisplay;
    private boolean showOnlySeconds;
    private boolean showBackground;

    public TNTimerConfigScreen(Screen parent) {
        super(Component.translatable("tntimer.config.title"));
        this.parent = parent;
        this.config = TNTimerConfig.getInstance();
        this.enabled = config.enabled;
        this.displayMode = config.displayMode;
        this.position = config.position;
        this.maxTntDisplay = config.maxTntDisplay;
        this.showOnlySeconds = config.showOnlySeconds;
        this.showBackground = config.showBackground;
    }

    public static Screen create(Screen parent) {
        return new TNTimerConfigScreen(parent);
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = 40;
        int btnWidth = 200;
        int btnHeight = 20;
        int gap = 25;

        addRenderableWidget(CycleButton.onOffBuilder(enabled)
                .create(centerX - btnWidth / 2, y, btnWidth, btnHeight,
                        Component.translatable("tntimer.config.enabled.title"),
                        (btn, val) -> this.enabled = val));
        y += gap;

        addRenderableWidget(CycleButton.<TNTimerConfig.DisplayMode>builder(TNTimerConfig.DisplayMode::getDisplayName, displayMode)
                .withValues(TNTimerConfig.DisplayMode.values())
                .create(centerX - btnWidth / 2, y, btnWidth, btnHeight,
                        Component.translatable("tntimer.config.display_mode.title"),
                        (btn, val) -> this.displayMode = val));
        y += gap;

        addRenderableWidget(CycleButton.<TNTimerConfig.Position>builder(TNTimerConfig.Position::getDisplayName, position)
                .withValues(TNTimerConfig.Position.values())
                .create(centerX - btnWidth / 2, y, btnWidth, btnHeight,
                        Component.translatable("tntimer.config.position.title"),
                        (btn, val) -> this.position = val));
        y += gap;

        addRenderableWidget(CycleButton.<Integer>builder(val -> Component.literal(String.valueOf(val)), maxTntDisplay)
                .withValues(IntStream.rangeClosed(1, 20).boxed().collect(Collectors.toList()))
                .create(centerX - btnWidth / 2, y, btnWidth, btnHeight,
                        Component.translatable("tntimer.config.max_tnt.title"),
                        (btn, val) -> this.maxTntDisplay = val));
        y += gap;

        addRenderableWidget(CycleButton.onOffBuilder(showOnlySeconds)
                .create(centerX - btnWidth / 2, y, btnWidth, btnHeight,
                        Component.translatable("tntimer.config.show_only_seconds.title"),
                        (btn, val) -> this.showOnlySeconds = val));
        y += gap;

        addRenderableWidget(CycleButton.onOffBuilder(showBackground)
                .create(centerX - btnWidth / 2, y, btnWidth, btnHeight,
                        Component.translatable("tntimer.config.show_background.title"),
                        (btn, val) -> this.showBackground = val));
        y += gap + 10;

        addRenderableWidget(Button.builder(Component.translatable("gui.done"),
                btn -> { applyAndSave(); onClose(); })
                .bounds(centerX - btnWidth / 2, y, btnWidth / 2 - 5, btnHeight).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"),
                btn -> onClose())
                .bounds(centerX + 5, y, btnWidth / 2 - 5, btnHeight).build());
    }

    private void applyAndSave() {
        config.enabled = this.enabled;
        config.displayMode = this.displayMode;
        config.position = this.position;
        config.maxTntDisplay = this.maxTntDisplay;
        config.showOnlySeconds = this.showOnlySeconds;
        config.showBackground = this.showBackground;
        config.save();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreenAndShow(parent);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
    }
}
