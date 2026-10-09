package com.g2806.tntimer.forge;

import com.g2806.tntimer.TNTWorldRenderer;
import com.g2806.tntimer.TNTimer;
import com.g2806.tntimer.TNTimerHudRenderer;
import com.g2806.tntimer.TNTimerKeys;
import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.client.renderer.entity.RenderTNTPrimed;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityTNTPrimed;

import java.util.EnumSet;

/**
 * Forge glue for 1.5. Forge here has no Mixin and no config-button API, so the HUD and 3D
 * labels hook Forge's render events and the settings screen opens with the K key.
 */
@Mod(modid = TNTimer.MOD_ID, useMetadata = true)
public final class TNTimerForge {

    private static final String[] LANGUAGES = {
            "en_US", "de_DE", "es_ES", "es_MX", "fr_FR", "it_IT", "ja_JP", "pl_PL", "ru_RU", "zh_CN"};

    @Mod.PreInit
    public void preInit(FMLPreInitializationEvent event) {
        ForgePlatformHelper.configDirectory = event.getModConfigurationDirectory().toPath();
        // No resource packs before 1.6: register the translations with FML directly.
        for (String lang : LANGUAGES) {
            LanguageRegistry.instance().loadLocalization("/assets/tntimer/lang/" + lang + ".lang", lang, false);
        }
        TNTimer.init();
        KeyBindingRegistry.registerKeyBinding(new OpenConfigKey());
        // Forge 1.5's render events can't be subscribed to, so the HUD is drawn at the end of
        // each render tick and the 3D labels from a wrapper around the primed TNT renderer.
        TickRegistry.registerTickHandler(new RenderTicks(), Side.CLIENT);
        RenderingRegistry.registerEntityRenderingHandler(EntityTNTPrimed.class, new TimedTNTRender());
    }

    /** Set at the start of every frame; the first primed TNT drawn that frame draws all labels. */
    private static boolean labelsDrawn;

    private static final class RenderTicks implements ITickHandler {
        @Override
        public void tickStart(EnumSet<TickType> type, Object... tickData) {
            labelsDrawn = false;
        }

        @Override
        public void tickEnd(EnumSet<TickType> type, Object... tickData) {
            TNTimerHudRenderer.render();
        }

        @Override
        public EnumSet<TickType> ticks() {
            return EnumSet.of(TickType.RENDER);
        }

        @Override
        public String getLabel() {
            return "TNTimer render";
        }
    }

    /** Vanilla primed TNT renderer that also draws the countdown labels (camera-relative). */
    private static final class TimedTNTRender extends RenderTNTPrimed {
        @Override
        public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
            renderPrimedTNT((EntityTNTPrimed) entity, x, y, z, yaw, partialTicks);
            if (!labelsDrawn) {
                labelsDrawn = true;
                TNTWorldRenderer.render(partialTicks);
            }
        }
    }

    /** FML 1.5 delivers key presses through a key handler instead of polling in a tick event. */
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
