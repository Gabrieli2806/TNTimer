package com.g2806.tntimer.forge;

import com.g2806.tntimer.platform.IPlatformHelper;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.common.Loader;

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
        Object deobf = Launch.blackboard.get("fml.deobfuscatedEnvironment");
        return deobf instanceof Boolean && (Boolean) deobf;
    }
}
