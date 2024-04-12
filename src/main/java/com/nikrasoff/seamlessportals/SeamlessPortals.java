package com.nikrasoff.seamlessportals;

import com.nikrasoff.seamlessportals.portals.PortalManager;
import net.fabricmc.api.ModInitializer;

import java.util.logging.Logger;

public class SeamlessPortals implements ModInitializer {
    public static PortalManager portalManager = new PortalManager();
    public static final String MOD_ID = "seamlessportals";
    public static final Logger LOGGER = Logger.getLogger(MOD_ID);
    public static boolean debugMode = true;

    @Override
    public void onInitialize() {
        System.out.println("Initialising Seamless Portals!");
        SeamlessPortalsBlockEvents.registerSeamlessPortalsBlockEvents();
        SeamlessPortalsCustomBlocks.registerCustomBlocks();
    }
}
