package com.nikrasoff.seamlessportals;

import com.nikrasoff.seamlessportals.blockevents.BlockActionDeregisterPortalGen;
import com.nikrasoff.seamlessportals.blockevents.BlockActionDestroyPortalsInRadius;
import com.nikrasoff.seamlessportals.blockevents.BlockActionFaceAwayFromPlayer;
import com.nikrasoff.seamlessportals.blockevents.BlockActionRegisterPortalGen;
import finalforeach.cosmicreach.blockevents.BlockEvents;

public class SeamlessPortalsBlockEvents {
    static void registerSeamlessPortalsBlockEvents(){
        SeamlessPortals.LOGGER.info("Registering block events from Seamless Portals!");
        BlockEvents.registerBlockEventAction(BlockActionFaceAwayFromPlayer.class);
        BlockEvents.registerBlockEventAction(BlockActionRegisterPortalGen.class);
        BlockEvents.registerBlockEventAction(BlockActionDeregisterPortalGen.class);
        BlockEvents.registerBlockEventAction(BlockActionDestroyPortalsInRadius.class);
    }
}
