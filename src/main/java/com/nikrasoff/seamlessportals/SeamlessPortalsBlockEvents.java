package com.nikrasoff.seamlessportals;

import com.nikrasoff.seamlessportals.blockevents.BlockEventActionDeregisterPortalGen;
import com.nikrasoff.seamlessportals.blockevents.BlockEventActionDestroyPortalsInRadius;
import com.nikrasoff.seamlessportals.blockevents.BlockEventActionFaceAwayFromPlayer;
import com.nikrasoff.seamlessportals.blockevents.BlockEventActionRegisterPortalGen;
import finalforeach.cosmicreach.blockevents.BlockEvents;

public class SeamlessPortalsBlockEvents {
    static void registerSeamlessPortalsBlockEvents(){
        SeamlessPortals.LOGGER.info("Registering custom Seamless Portals block events!");
        BlockEvents.registerBlockEventAction(new BlockEventActionFaceAwayFromPlayer());
        BlockEvents.registerBlockEventAction(new BlockEventActionRegisterPortalGen());
        BlockEvents.registerBlockEventAction(new BlockEventActionDeregisterPortalGen());
        BlockEvents.registerBlockEventAction(new BlockEventActionDestroyPortalsInRadius());
    }
}
