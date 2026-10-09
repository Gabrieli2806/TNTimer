package com.g2806.tntimer.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import cpw.mods.fml.client.IModGuiFactory;

import java.util.Set;

/** "Config" button in Forge's mod list. */
public class TNTimerGuiFactory implements IModGuiFactory {

    @Override
    public void initialize(Minecraft minecraft) {
    }

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return ConfigScreen.class;
    }

    @Override
    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
        return null;
    }

    /** Forge before 1.12 instantiates the config screen reflectively with the parent screen. */
    public static final class ConfigScreen extends com.g2806.tntimer.TNTimerConfigScreen {
        public ConfigScreen(GuiScreen parent) {
            super(parent);
        }
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }
}
