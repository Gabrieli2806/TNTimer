package com.g2806.tntimer;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TNTimer implements ModInitializer {
	public static final String MOD_ID = "tntimer";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Hello, Gabrieli2806 was here!");
		new TNTCountdown().onInitializeClient();
	}
}
