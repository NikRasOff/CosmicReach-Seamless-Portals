package com.nikrasoff.seamlessportals;

import com.nikrasoff.seamlessportals.blocks.blockevents.BlockActionDeregisterPortalGen;
import com.nikrasoff.seamlessportals.blocks.blockevents.BlockActionDestroyPortalsInRadius;
import com.nikrasoff.seamlessportals.blocks.blockevents.BlockActionRegisterPortalGen;
import finalforeach.cosmicreach.gameevents.blockevents.BlockEvents;

public class SeamlessPortalsBlockEvents {
    static void registerSeamlessPortalsBlockEvents(){
        SeamlessPortals.LOGGER.info("Registering block events from Seamless Portals!");
        BlockEvents.registerBlockEventAction(BlockActionRegisterPortalGen.class);
        BlockEvents.registerBlockEventAction(BlockActionDeregisterPortalGen.class);
        BlockEvents.registerBlockEventAction(BlockActionDestroyPortalsInRadius.class);
    }
}
