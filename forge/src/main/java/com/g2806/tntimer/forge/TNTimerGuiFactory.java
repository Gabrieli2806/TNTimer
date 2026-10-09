package com.g2806.tntimer.forge;

import com.g2806.tntimer.TNTimer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;

import java.util.Set;

/**
 * "Config" button in Forge's mod list. Forge 1.11.2 is mid-transition and requires both the
 * old (mainConfigGuiClass/getHandlerFor) and new (hasConfigGui/createConfigGui) methods.
 */
public class TNTimerGuiFactory implements IModGuiFactory {

    @Override
    public void initialize(Minecraft minecraft) {
    }

    @Override
    public boolean hasConfigGui() {
        return true;
    }

    @Override
    public GuiScreen createConfigGui(GuiScreen parent) {
        return TNTimer.createConfigScreen(parent);
    }

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return null;
    }

    @Override
    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
        return null;
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }
}
