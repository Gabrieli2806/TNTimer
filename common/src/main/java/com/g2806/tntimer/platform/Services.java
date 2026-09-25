package com.g2806.tntimer.platform;

import java.util.ServiceLoader;

public final class Services {

    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);

    private Services() {
    }

    public static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No service implementation found for " + clazz.getName()));
    }
}
