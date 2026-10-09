package com.g2806.tntimer.forge;

import com.g2806.tntimer.platform.IPlatformHelper;
import cpw.mods.fml.common.Loader;

import java.nio.file.Path;

public class ForgePlatformHelper implements IPlatformHelper {

    /** Set from FMLPreInitializationEvent, before the config is first loaded. */
    static Path configDirectory;

    @Override
    public String getPlatformName() {
        return "Forge";
    }

    @Override
    public Path getConfigDirectory() {
        return configDirectory != null ? configDirectory : Loader.instance().getConfigDir().toPath();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return Loader.isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        // No launchwrapper blackboard in 1.5: deobfuscated (MCP) class names only exist in dev.
        try {
            Class.forName("net.minecraft.world.World", false, ForgePlatformHelper.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
