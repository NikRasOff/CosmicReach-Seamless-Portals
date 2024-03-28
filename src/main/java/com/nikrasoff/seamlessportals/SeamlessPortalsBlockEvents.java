package com.nikrasoff.seamlessportals;

import finalforeach.cosmicreach.world.blockevents.BlockEvents;

public class SeamlessPortalsBlockEvents {
    static void registerSeamlessPortalsBlockEvents(){
        SeamlessPortals.LOGGER.info("Registering custom Seamless Portals block events!");
        BlockEvents.registerBlockEventAction(new BlockEventActionFaceAwayFromPlayer());
        BlockEvents.registerBlockEventAction(new BlockEventActionRegisterPortalGen());
        BlockEvents.registerBlockEventAction(new BlockEventActionDeregisterPortalGen());
        BlockEvents.registerBlockEventAction(new BlockEventActionDestroyPortalsInRadius());
    }
}
