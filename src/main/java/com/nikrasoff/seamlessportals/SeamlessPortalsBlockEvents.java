package com.nikrasoff.seamlessportals;

import com.nikrasoff.seamlessportals.blockevents.BlockEventActionDeregisterPortalGen;
import com.nikrasoff.seamlessportals.blockevents.BlockEventActionDestroyPortalsInRadius;
import com.nikrasoff.seamlessportals.blockevents.BlockEventActionFaceAwayFromPlayer;
import com.nikrasoff.seamlessportals.blockevents.BlockEventActionRegisterPortalGen;
import finalforeach.cosmicreach.blockevents.BlockEvents;

public class SeamlessPortalsBlockEvents {
    static void registerSeamlessPortalsBlockEvents(){
        System.out.println("Registering custom block events from Seamless Portals!");
        BlockEvents.registerBlockEventAction(new BlockEventActionFaceAwayFromPlayer());
        BlockEvents.registerBlockEventAction(new BlockEventActionRegisterPortalGen());
        BlockEvents.registerBlockEventAction(new BlockEventActionDeregisterPortalGen());
        BlockEvents.registerBlockEventAction(new BlockEventActionDestroyPortalsInRadius());
    }
}
