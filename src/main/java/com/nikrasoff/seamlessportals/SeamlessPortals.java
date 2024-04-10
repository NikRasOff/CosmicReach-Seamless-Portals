package com.nikrasoff.seamlessportals;

import com.nikrasoff.seamlessportals.portals.PortalManager;
import org.coolcosmos.cosmicquilt.api.entrypoint.ModInitializer;
import org.quiltmc.loader.api.ModContainer;

import java.util.logging.Logger;

public class SeamlessPortals implements ModInitializer {
    public static PortalManager portalManager = new PortalManager();
    public static final String MOD_ID = "seamlessportals";
    public static final Logger LOGGER = Logger.getLogger(MOD_ID);
    public static boolean debugMode = false;

    @Override
    public void onInitialize(ModContainer mod) {
        System.out.println("Initialising Seamless Portals!");
        SeamlessPortalsBlockEvents.registerSeamlessPortalsBlockEvents();
        SeamlessPortalsCustomBlocks.registerCustomBlocks();
    }
}
