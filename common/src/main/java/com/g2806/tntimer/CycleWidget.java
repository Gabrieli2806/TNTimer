package com.g2806.tntimer;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Button that cycles through a fixed set of values and shows "Label: value".
 * Minecraft 1.16 has no CycleButton (added in 1.17), so the settings screen uses this instead.
 */
public final class CycleWidget<T> extends Button {

    private final Component label;
    private final T[] values;
    private final Function<T, Component> display;
    private final Consumer<T> onChange;
    private int index;

    public CycleWidget(int x, int y, int width, int height, Component label, T[] values, T initial,
                       Function<T, Component> display, Consumer<T> onChange) {
        super(x, y, width, height, TextComponent.EMPTY, button -> { });
        this.label = label;
        this.values = values;
        this.display = display;
        this.onChange = onChange;
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(initial)) index = i;
        }
        updateMessage();
    }

    /** On/off toggle using vanilla's "ON"/"OFF" strings. */
    public static CycleWidget<Boolean> onOff(int x, int y, int width, int height, Component label,
                                             boolean initial, Consumer<Boolean> onChange) {
        return new CycleWidget<>(x, y, width, height, label, new Boolean[]{true, false}, initial,
                value -> new TranslatableComponent(value ? "options.on" : "options.off"), onChange);
    }

    @Override
    public void onPress() {
        index = (index + 1) % values.length;
        updateMessage();
        onChange.accept(values[index]);
    }

    private void updateMessage() {
        setMessage(new TextComponent("").append(label).append(": ").append(display.apply(values[index])));
    }
}
