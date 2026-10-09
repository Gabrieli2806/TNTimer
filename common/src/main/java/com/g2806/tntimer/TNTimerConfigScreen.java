package com.g2806.tntimer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;

/**
 * Settings screen: general options on the left, options for the selected display mode on
 * the right (stacked into one column on narrow windows). Edits go to a working copy and are
 * only saved on "Done". Same layout as the modern versions, built from legacy widgets.
 */
public class TNTimerConfigScreen extends GuiScreen {

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

    private static final int ID_RESET = 1000;
    private static final int ID_CANCEL = 1001;
    private static final int ID_DONE = 1002;

    private final GuiScreen parent;
    private final TNTimerConfig working;

    private final List<GuiButton> hudWidgets = new ArrayList<>();
    private final Map<GuiButton, String> tooltips = new HashMap<>();
    private int nextId;

    private int leftX;
    private int rightX;
    private int rightTop;
    private int leftBottom;
    private int hudBottom;
    private int worldBottom;
    private boolean twoColumns;

    public TNTimerConfigScreen(GuiScreen parent) {
        this.parent = parent;
        this.working = TNTimerConfig.getInstance().copy();
    }

    @Override
    public void initGui() {
        buttonList.clear();
        hudWidgets.clear();
        tooltips.clear();
        nextId = 0;

        twoColumns = this.width >= COLUMN_WIDTH * 2 + COLUMN_GAP + 2 * PANEL_PAD + 20;
        int totalWidth = twoColumns ? COLUMN_WIDTH * 2 + COLUMN_GAP : COLUMN_WIDTH;
        leftX = (this.width - totalWidth) / 2;
        rightX = twoColumns ? leftX + COLUMN_WIDTH + COLUMN_GAP : leftX;

        // ---- General ----
        int y = TOP + HEADER_HEIGHT;
        y = addRow(onOff(leftX, y, "enabled", working.enabled, val -> working.enabled = val), y);
        y = addRow(withTooltip(new CycleButton<>(nextId++, leftX, y, I18n.format("tntimer.config.display_mode.title"),
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
        hy = addRow(track(withTooltip(new CycleButton<>(nextId++, rightX, hy, I18n.format("tntimer.config.position.title"),
                TNTimerConfig.Position.values(), working.position, TNTimerConfig.Position::getDisplayName,
                val -> working.position = val), "position")), hy);
        hy = addRow(track(onOff(rightX, hy, "show_background", working.showBackground,
                val -> working.showBackground = val)), hy);
        hy = addRow(track(slider(rightX, hy, "hud_scale",
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

        GuiButton reset = new GuiButton(ID_RESET, leftX, footerY, buttonWidth, ROW_HEIGHT,
                I18n.format("tntimer.config.reset"));
        buttonList.add(withTooltip(reset, "reset"));
        buttonList.add(new GuiButton(ID_CANCEL, leftX + buttonWidth + gap, footerY, buttonWidth, ROW_HEIGHT,
                I18n.format("gui.cancel")));
        buttonList.add(new GuiButton(ID_DONE, leftX + (buttonWidth + gap) * 2, footerY, buttonWidth, ROW_HEIGHT,
                I18n.format("gui.done")));

        updateModeWidgets();
    }

    private int addRow(GuiButton widget, int y) {
        buttonList.add(widget);
        return y + ROW_HEIGHT + ROW_GAP;
    }

    private GuiButton track(GuiButton widget) {
        hudWidgets.add(widget);
        return widget;
    }

    private CycleButton<Boolean> onOff(int x, int y, String key, boolean initial, Consumer<Boolean> setter) {
        return withTooltip(new CycleButton<>(nextId++, x, y, I18n.format("tntimer.config." + key + ".title"),
                new Boolean[]{true, false}, initial,
                value -> I18n.format(value ? "options.on" : "options.off"), setter), key);
    }

    private IntSlider slider(int x, int y, String key, int min, int max, int step, int initial,
                             IntFunction<String> formatter, IntConsumer onChange) {
        return withTooltip(new IntSlider(nextId++, x, y, I18n.format("tntimer.config." + key + ".title"),
                min, max, step, initial, formatter, onChange), key);
    }

    private <T extends GuiButton> T withTooltip(T widget, String key) {
        tooltips.put(widget, I18n.format("tntimer.config." + key + ".tooltip"));
        return widget;
    }

    private boolean hudMode() {
        return working.displayMode == TNTimerConfig.DisplayMode.HUD;
    }

    private void updateModeWidgets() {
        boolean hud = hudMode();
        for (GuiButton widget : hudWidgets) {
            widget.visible = hud;
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button instanceof CycleButton) {
            ((CycleButton<?>) button).cycle();
        } else if (button.id == ID_RESET) {
            working.copyFrom(new TNTimerConfig());
            initGui();
        } else if (button.id == ID_CANCEL) {
            close();
        } else if (button.id == ID_DONE) {
            working.sanitize();
            TNTimerConfig config = TNTimerConfig.getInstance();
            config.copyFrom(working);
            config.save();
            close();
        }
    }

    private void close() {
        this.mc.displayGuiScreen(parent);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        int modeBottom = hudMode() ? hudBottom : worldBottom;
        if (twoColumns) {
            int bottom = Math.max(leftBottom, modeBottom);
            drawPanel(leftX, TOP, bottom);
            drawPanel(rightX, rightTop, bottom);
        } else {
            drawPanel(leftX, TOP, leftBottom);
            drawPanel(rightX, rightTop, modeBottom);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);

        drawCenteredString(this.fontRendererObj, I18n.format("tntimer.config.title"), this.width / 2, 12, TITLE_COLOR);
        drawCenteredString(this.fontRendererObj, I18n.format("tntimer.config.subtitle"), this.width / 2, 23, SUBTITLE_COLOR);
        drawString(this.fontRendererObj, I18n.format("tntimer.config.section.general"), leftX, TOP + 2, HEADER_COLOR);
        drawString(this.fontRendererObj, I18n.format(hudMode() ? "tntimer.config.section.hud" : "tntimer.config.section.world"),
                rightX, rightTop + 2, HEADER_COLOR);
        if (!hudMode()) {
            this.fontRendererObj.drawSplitString(I18n.format("tntimer.config.section.world.info"),
                    rightX, rightTop + HEADER_HEIGHT + 2, COLUMN_WIDTH, SUBTITLE_COLOR);
        }

        for (Map.Entry<GuiButton, String> entry : tooltips.entrySet()) {
            GuiButton widget = entry.getKey();
            if (widget.visible && widget.isMouseOver()) {
                drawHoveringText(this.fontRendererObj.listFormattedStringToWidth(entry.getValue(), 200), mouseX, mouseY);
                GlStateManager.disableLighting();
                break;
            }
        }
    }

    private void drawPanel(int x, int top, int bottom) {
        int x0 = x - PANEL_PAD;
        int y0 = top - PANEL_PAD;
        int x1 = x + COLUMN_WIDTH + PANEL_PAD;
        int y1 = bottom + PANEL_PAD - ROW_GAP;
        drawRect(x0, y0, x1, y1, PANEL_COLOR);
        drawRect(x0, y0, x1, y0 + 1, PANEL_BORDER);
        drawRect(x0, y1 - 1, x1, y1, PANEL_BORDER);
        drawRect(x0, y0 + 1, x0 + 1, y1 - 1, PANEL_BORDER);
        drawRect(x1 - 1, y0 + 1, x1, y1 - 1, PANEL_BORDER);
    }

    /** Button that cycles through a fixed set of values and shows "Label: value". */
    private static final class CycleButton<T> extends GuiButton {
        private final String label;
        private final T[] values;
        private final Function<T, String> display;
        private final Consumer<T> onChange;
        private int index;

        CycleButton(int id, int x, int y, String label, T[] values, T initial,
                    Function<T, String> display, Consumer<T> onChange) {
            super(id, x, y, COLUMN_WIDTH, ROW_HEIGHT, "");
            this.label = label;
            this.values = values;
            this.display = display;
            this.onChange = onChange;
            for (int i = 0; i < values.length; i++) {
                if (values[i].equals(initial)) index = i;
            }
            updateText();
        }

        void cycle() {
            index = (index + 1) % values.length;
            updateText();
            onChange.accept(values[index]);
        }

        private void updateText() {
            this.displayString = label + ": " + display.apply(values[index]);
        }
    }

    /** Integer slider that snaps to a fixed step and shows "Label: value". */
    private static final class IntSlider extends GuiButton {
        private final String label;
        private final int min;
        private final int max;
        private final int step;
        private final IntFunction<String> formatter;
        private final IntConsumer onChange;
        private float value;
        private boolean dragging;

        IntSlider(int id, int x, int y, String label, int min, int max, int step, int initial,
                  IntFunction<String> formatter, IntConsumer onChange) {
            super(id, x, y, COLUMN_WIDTH, ROW_HEIGHT, "");
            this.label = label;
            this.min = min;
            this.max = max;
            this.step = step;
            this.formatter = formatter;
            this.onChange = onChange;
            this.value = (MathHelper.clamp_int(initial, min, max) - min) / (float) (max - min);
            updateText();
        }

        private int currentValue() {
            int raw = min + Math.round(value * (max - min));
            int snapped = min + Math.round((raw - min) / (float) step) * step;
            return MathHelper.clamp_int(snapped, min, max);
        }

        private void updateText() {
            this.displayString = label + ": " + formatter.apply(currentValue());
        }

        private void setFromMouse(int mouseX) {
            value = MathHelper.clamp_float((mouseX - (this.xPosition + 4)) / (float) (this.width - 8),  0.0F, 1.0F);
            updateText();
            onChange.accept(currentValue());
        }

        @Override
        protected int getHoverState(boolean mouseOver) {
            return 0; // slider track, like vanilla's option sliders
        }

        @Override
        protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
            if (!this.visible) return;
            if (dragging) setFromMouse(mouseX);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            int knobX = this.xPosition + (int) (value * (this.width - 8));
            drawTexturedModalRect(knobX, this.yPosition, 0, 66, 4, 20);
            drawTexturedModalRect(knobX + 4, this.yPosition, 196, 66, 4, 20);
        }

        @Override
        public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
            if (super.mousePressed(mc, mouseX, mouseY)) {
                dragging = true;
                setFromMouse(mouseX);
                return true;
            }
            return false;
        }

        @Override
        public void mouseReleased(int mouseX, int mouseY) {
            dragging = false;
        }
    }
}
