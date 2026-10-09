package com.g2806.tntimer.platform;

import java.nio.file.Path;

/**
 * Loader-specific behaviour used by the common module. Each loader module ships one
 * implementation, registered through META-INF/services and picked up by {@link Services}.
 */
public interface IPlatformHelper {

    /** Human readable loader name, e.g. "Fabric". */
    String getPlatformName();

    /** The game's config directory (where tntimer.json lives). */
    Path getConfigDirectory();

    /** Whether another mod is loaded. */
    boolean isModLoaded(String modId);

    /** Whether the game runs in a development environment. */
    boolean isDevelopmentEnvironment();
}
