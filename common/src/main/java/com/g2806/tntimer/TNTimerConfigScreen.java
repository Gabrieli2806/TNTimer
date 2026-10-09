package com.g2806.tntimer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;

/**
 * Settings screen: general options on the left, options for the selected display mode on
 * the right (stacked into one column on narrow windows). Edits go to a working copy and are
 * only saved on "Done".
 */
public class TNTimerConfigScreen extends Screen {

    private static final int ROW_HEIGHT = 20;
    private static final int ROW_GAP = 4;
    private static final int HEADER_HEIGHT = 14;
    private static final int COLUMN_WIDTH = 170;
    private static final int COLUMN_GAP = 16;
    private static final int PANEL_PAD = 6;
    private static final int TOP = 40;

    private static final int PANEL_COLOR = 0x90000000;
    private static final int PANEL_BORDER = 0x40FFFFFF;
    private static final int TITLE_COLOR = 0xFFFFFFFF;
    private static final int SUBTITLE_COLOR = 0xFFA0A0A0;
    private static final int HEADER_COLOR = 0xFFFFD27F;

    private final Screen parent;
    private final TNTimerConfig working;

    private final List<AbstractWidget> hudWidgets = new ArrayList<>();
    /** Tooltips are drawn by the screen itself: widgets have no tooltip API before 1.19.3. */
    private final Map<AbstractWidget, Component> tooltips = new HashMap<>();

    private int leftX;
    private int rightX;
    private int rightTop;
    private int leftBottom;
    private int hudBottom;
    private int worldBottom;
    private boolean twoColumns;

    public TNTimerConfigScreen(Screen parent) {
        super(new TranslatableComponent("tntimer.config.title"));
        this.parent = parent;
        this.working = TNTimerConfig.getInstance().copy();
    }

    @Override
    protected void init() {
        hudWidgets.clear();
        tooltips.clear();

        twoColumns = this.width >= COLUMN_WIDTH * 2 + COLUMN_GAP + 2 * PANEL_PAD + 20;
        int totalWidth = twoColumns ? COLUMN_WIDTH * 2 + COLUMN_GAP : COLUMN_WIDTH;
        leftX = (this.width - totalWidth) / 2;
        rightX = twoColumns ? leftX + COLUMN_WIDTH + COLUMN_GAP : leftX;

        // ---- General ----
        int y = TOP + HEADER_HEIGHT;
        y = addRow(onOff(leftX, y, "enabled", working.enabled, val -> working.enabled = val), y);
        y = addRow(withTooltip(new CycleWidget<>(leftX, y, COLUMN_WIDTH, ROW_HEIGHT,
                new TranslatableComponent("tntimer.config.display_mode.title"),
                TNTimerConfig.DisplayMode.values(), working.displayMode, TNTimerConfig.DisplayMode::getDisplayName,
                val -> {
                    working.displayMode = val;
                    updateModeWidgets();
                }), "display_mode"), y);
        y = addRow(slider(leftX, y, "max_tnt", 1, TNTimerConfig.MAX_TNT_DISPLAY, 1,
                working.maxTntDisplay, String::valueOf, val -> working.maxTntDisplay = val), y);
        y = addRow(onOff(leftX, y, "show_only_seconds", working.showOnlySeconds,
                val -> working.showOnlySeconds = val), y);
        leftBottom = y;

        // ---- Mode-specific: only the active mode's section is visible ----
        rightTop = twoColumns ? TOP : leftBottom + PANEL_PAD * 2 + 4;
        int modeY = rightTop + HEADER_HEIGHT;

        int hy = modeY;
        hy = addRow(track(hudWidgets, withTooltip(new CycleWidget<>(rightX, hy, COLUMN_WIDTH, ROW_HEIGHT,
                new TranslatableComponent("tntimer.config.position.title"),
                TNTimerConfig.Position.values(), working.position, TNTimerConfig.Position::getDisplayName,
                val -> working.position = val), "position")), hy);
        hy = addRow(track(hudWidgets, onOff(rightX, hy, "show_background", working.showBackground,
                val -> working.showBackground = val)), hy);
        hy = addRow(track(hudWidgets, slider(rightX, hy, "hud_scale",
                Math.round(TNTimerConfig.MIN_HUD_SCALE * 100), Math.round(TNTimerConfig.MAX_HUD_SCALE * 100), 10,
                Math.round(working.hudScale * 100), val -> val + "%", val -> working.hudScale = val / 100f)), hy);
        hudBottom = hy;

        // 3D mode has no extra options; its panel only holds a short note.
        worldBottom = modeY + ROW_HEIGHT + ROW_GAP;

        // ---- Footer: below the tallest column, but never off-screen ----
        int contentBottom = Math.max(twoColumns ? leftBottom : 0, Math.max(hudBottom, worldBottom));
        int footerY = Math.min(Math.max(contentBottom + PANEL_PAD + 8, this.height - 28), this.height - 24);
        int gap = 6;
        int buttonWidth = (totalWidth - 2 * gap) / 3;

        addButton(withTooltip(new Button(leftX, footerY, buttonWidth, ROW_HEIGHT,
                I18n.get("tntimer.config.reset"), btn -> {
                    working.copyFrom(new TNTimerConfig());
                    init(this.minecraft, this.width, this.height); // rebuild widgets with defaults
                }), "reset"));
        addButton(new Button(leftX + buttonWidth + gap, footerY, buttonWidth, ROW_HEIGHT,
                I18n.get("gui.cancel"), btn -> onClose()));
        addButton(new Button(leftX + (buttonWidth + gap) * 2, footerY, buttonWidth, ROW_HEIGHT,
                I18n.get("gui.done"), btn -> {
                    applyAndSave();
                    onClose();
                }));

        updateModeWidgets();
    }

    private int addRow(AbstractWidget widget, int y) {
        addButton(widget);
        return y + ROW_HEIGHT + ROW_GAP;
    }

    private static AbstractWidget track(List<AbstractWidget> group, AbstractWidget widget) {
        group.add(widget);
        return widget;
    }

    private CycleWidget<Boolean> onOff(int x, int y, String key, boolean initial, BooleanSetter setter) {
        return withTooltip(CycleWidget.onOff(x, y, COLUMN_WIDTH, ROW_HEIGHT,
                new TranslatableComponent("tntimer.config." + key + ".title"), initial, setter::set), key);
    }

    private IntSlider slider(int x, int y, String key, int min, int max, int step, int initial,
                             IntFunction<String> formatter, IntConsumer onChange) {
        return withTooltip(new IntSlider(x, y, COLUMN_WIDTH,
                new TranslatableComponent("tntimer.config." + key + ".title"),
                min, max, step, initial, formatter, onChange), key);
    }

    private <T extends AbstractWidget> T withTooltip(T widget, String key) {
        tooltips.put(widget, new TranslatableComponent("tntimer.config." + key + ".tooltip"));
        return widget;
    }

    private boolean hudMode() {
        return working.displayMode == TNTimerConfig.DisplayMode.HUD;
    }

    private void updateModeWidgets() {
        boolean hud = hudMode();
        hudWidgets.forEach(w -> w.visible = hud);
    }

    private void applyAndSave() {
        working.sanitize();
        TNTimerConfig config = TNTimerConfig.getInstance();
        config.copyFrom(working);
        config.save();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    /** Vanilla background plus the column panels. Before 1.20.2 screens draw it themselves. */
    private void renderPanels() {
        renderBackground();

        int modeBottom = hudMode() ? hudBottom : worldBottom;
        if (twoColumns) {
            int bottom = Math.max(leftBottom, modeBottom);
            drawPanel(leftX, TOP, bottom);
            drawPanel(rightX, rightTop, bottom);
        } else {
            drawPanel(leftX, TOP, leftBottom);
            drawPanel(rightX, rightTop, modeBottom);
        }
    }

    private static void drawPanel(int x, int top, int bottom) {
        int x0 = x - PANEL_PAD;
        int y0 = top - PANEL_PAD;
        int x1 = x + COLUMN_WIDTH + PANEL_PAD;
        int y1 = bottom + PANEL_PAD - ROW_GAP;
        fill(x0, y0, x1, y1, PANEL_COLOR);
        // 1px border (no outline helper before 1.20)
        fill(x0, y0, x1, y0 + 1, PANEL_BORDER);
        fill(x0, y1 - 1, x1, y1, PANEL_BORDER);
        fill(x0, y0 + 1, x0 + 1, y1 - 1, PANEL_BORDER);
        fill(x1 - 1, y0 + 1, x1, y1 - 1, PANEL_BORDER);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTick) {
        renderPanels();
        super.render(mouseX, mouseY, partialTick);

        drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 12, TITLE_COLOR);
        drawCenteredString(this.font, I18n.get("tntimer.config.subtitle"), this.width / 2, 23, SUBTITLE_COLOR);

        drawString(this.font, I18n.get("tntimer.config.section.general"), leftX, TOP + 2, HEADER_COLOR);
        drawString(this.font, I18n.get(hudMode() ? "tntimer.config.section.hud" : "tntimer.config.section.world"),
                rightX, rightTop + 2, HEADER_COLOR);
        if (!hudMode()) {
            int lineY = rightTop + HEADER_HEIGHT + 2;
            for (String line : this.font.split(I18n.get("tntimer.config.section.world.info"), COLUMN_WIDTH)) {
                this.font.drawShadow(line, rightX, lineY, SUBTITLE_COLOR);
                lineY += this.font.lineHeight;
            }
        }

        for (Map.Entry<AbstractWidget, Component> entry : tooltips.entrySet()) {
            AbstractWidget widget = entry.getKey();
            if (widget.visible && widget.isMouseOver(mouseX, mouseY)) {
                renderTooltip(this.font.split(entry.getValue().getString(), 200), mouseX, mouseY);
                break;
            }
        }
    }

    @FunctionalInterface
    private interface BooleanSetter {
        void set(boolean value);
    }

    /** Integer slider that snaps to a fixed step and shows "Label: value". */
    private static final class IntSlider extends AbstractSliderButton {
        private final Component label;
        private final int min;
        private final int max;
        private final int step;
        private final IntFunction<String> formatter;
        private final IntConsumer onChange;

        IntSlider(int x, int y, int width, Component label, int min, int max, int step, int initial,
                  IntFunction<String> formatter, IntConsumer onChange) {
            super(Minecraft.getInstance().options, x, y, width, ROW_HEIGHT,
                    (Mth.clamp(initial, min, max) - min) / (double) (max - min));
            this.label = label;
            this.min = min;
            this.max = max;
            this.step = step;
            this.formatter = formatter;
            this.onChange = onChange;
            updateMessage();
        }

        private int currentValue() {
            int raw = min + (int) Math.round(this.value * (max - min));
            int snapped = min + Math.round((raw - min) / (float) step) * step;
            return Mth.clamp(snapped, min, max);
        }

        @Override
        protected void updateMessage() {
            setMessage(label.getString() + ": " + formatter.apply(currentValue()));
        }

        @Override
        protected void applyValue() {
            onChange.accept(currentValue());
        }
    }
}
