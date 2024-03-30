package com.nikrasoff.seamlessportals;

import dev.crmodders.flux.tags.Identifier;
import dev.crmodders.flux.util.BlockBuilderUtils;

public class SeamlessPortalsCustomBlocks {
    public static void registerCustomBlocks(){
        BlockBuilderUtils.getBlockFromJson(new Identifier(SeamlessPortals.MOD_ID, "portal_generator"));
        BlockBuilderUtils.getBlockFromJson(new Identifier(SeamlessPortals.MOD_ID, "portal_destabiliser"));
        BlockBuilderUtils.getBlockFromJson(new Identifier(SeamlessPortals.MOD_ID, "ph_portal"));
        BlockBuilderUtils.getBlockFromJson(new Identifier(SeamlessPortals.MOD_ID, "ph_destabiliser_pulse"));
    }
}
