package com.nikrasoff.seamlessportals;

import dev.crmodders.flux.api.generators.BlockGenerator;
import dev.crmodders.flux.registry.FluxRegistries;
import dev.crmodders.flux.tags.Identifier;

import static com.nikrasoff.seamlessportals.SeamlessPortals.MOD_ID;

public class SeamlessPortalsCustomBlocks {
    static String[] blockIds = {
            "portal_generator",
            "portal_destabiliser",
            "ph_portal",
            "ph_destabiliser_pulse"
    };
    public static void registerCustomBlocks(){
        System.out.println("Registering custom blocks from Seamless Portals!");
        for (String block : blockIds){
            FluxRegistries.BLOCKS.register(new Identifier(MOD_ID, block), BlockGenerator::createGenerator);
        }
    }
}
