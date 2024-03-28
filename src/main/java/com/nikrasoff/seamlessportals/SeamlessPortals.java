package com.nikrasoff.seamlessportals;

import net.fabricmc.api.ModInitializer;

import java.util.logging.Logger;

public class SeamlessPortals implements ModInitializer {
    public static PortalManager portalManager = new PortalManager();
    public static final String MOD_ID = "seamlessportals";
    public static final Logger LOGGER = Logger.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Seamless Portals!");
        SeamlessPortalsBlockEvents.registerSeamlessPortalsBlockEvents();
    }
}
