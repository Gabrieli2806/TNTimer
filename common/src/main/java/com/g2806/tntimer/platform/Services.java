package com.g2806.tntimer.platform;

import java.util.ServiceLoader;

public final class Services {

    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);

    private Services() {
    }

    public static <T> T load(Class<T> clazz) {
        // Use our own class loader: on (Neo)Forge the thread context loader can't see mod classes.
        return ServiceLoader.load(clazz, Services.class.getClassLoader())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No service implementation found for " + clazz.getName()));
    }
}
