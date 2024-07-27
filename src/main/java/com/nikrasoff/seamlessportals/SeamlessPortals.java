package com.nikrasoff.seamlessportals;

import com.nikrasoff.seamlessportals.items.HandheldPortalGen;
import com.nikrasoff.seamlessportals.models.EntityItemModel;
import com.nikrasoff.seamlessportals.portals.Portal;
import com.nikrasoff.seamlessportals.portals.PortalManager;
import dev.crmodders.cosmicquilt.api.entrypoint.ModInitializer;
import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.blocks.BlockStateGenerator;
import finalforeach.cosmicreach.entities.EntityCreator;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.rendering.items.ItemRenderer;
import org.quiltmc.loader.api.ModContainer;

import java.util.logging.Logger;

public class SeamlessPortals implements ModInitializer {
    public static PortalManager portalManager = new PortalManager();
    public static final String MOD_ID = "seamlessportals";
    public static final Logger LOGGER = Logger.getLogger(MOD_ID);

    static String[] blockIds = {
            "portal_generator",
            "portal_destabiliser",
            "ph_portal",
            "ph_destabiliser_pulse"
    };
    @Override
    public void onInitialize(ModContainer modContainer) {
        LOGGER.info("Initialising Seamless Portals!");

        SeamlessPortalsBlockEvents.registerSeamlessPortalsBlockEvents();

        for (String id: blockIds){
            Block.getInstance(id);
        }

        EntityCreator.registerEntityCreator("seamlessportals:entity_portal", Portal::readPortal);
        ItemRenderer.registerItemModelCreator(HandheldPortalGen.class, (handheldPortalGen) -> new EntityItemModel("handheld_portal_gen.json", "handheld_portal_gen.animation.json",
                "animation.handheld_portal_generator.idle", "handheld_portal_gen.png"));

        Item.registerItem(new HandheldPortalGen());
    }
}
