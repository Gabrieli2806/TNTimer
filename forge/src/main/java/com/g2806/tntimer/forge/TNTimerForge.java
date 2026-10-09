package com.g2806.tntimer.forge;

import com.g2806.tntimer.TNTWorldRenderer;
import com.g2806.tntimer.TNTimer;
import com.g2806.tntimer.TNTimerHudRenderer;
import com.g2806.tntimer.TNTimerKeys;
import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;

import java.util.EnumSet;

/**
 * Forge glue for 1.6. Forge here has no Mixin and no config-button API, so the HUD and 3D
 * labels hook Forge's render events and the settings screen opens with the K key.
 */
@Mod(modid = TNTimer.MOD_ID, useMetadata = true)
public final class TNTimerForge {

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ForgePlatformHelper.configDirectory = event.getModConfigurationDirectory().toPath();
        TNTimer.init();
        KeyBindingRegistry.registerKeyBinding(new OpenConfigKey());
        MinecraftForge.EVENT_BUS.register(this);
    }

    @ForgeSubscribe
    public void onOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type == RenderGameOverlayEvent.ElementType.ALL) {
            TNTimerHudRenderer.render();
        }
    }

    @ForgeSubscribe
    public void onWorldRendered(RenderWorldLastEvent event) {
        TNTWorldRenderer.render(event.partialTicks);
    }

    /** FML 1.6 delivers key presses through a key handler instead of polling in a tick event. */
    private static final class OpenConfigKey extends KeyBindingRegistry.KeyHandler {
        OpenConfigKey() {
            super(new KeyBinding[]{TNTimerKeys.OPEN_CONFIG}, new boolean[]{false});
        }

        @Override
        public void keyDown(EnumSet<TickType> types, KeyBinding binding, boolean tickEnd, boolean isRepeat) {
            if (tickEnd && Minecraft.getMinecraft().currentScreen == null) TNTimer.openConfigScreen();
        }

        @Override
        public void keyUp(EnumSet<TickType> types, KeyBinding binding, boolean tickEnd) {
        }

        @Override
        public EnumSet<TickType> ticks() {
            return EnumSet.of(TickType.CLIENT);
        }

        @Override
        public String getLabel() {
            return "TNTimer keys";
        }
    }
}
