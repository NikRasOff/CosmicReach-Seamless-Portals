package com.nikrasoff.seamlessportals;

import com.badlogic.gdx.math.Vector3;
import com.nikrasoff.seamlessportals.items.HandheldPortalGen;
import com.nikrasoff.seamlessportals.models.EntityItemModel;
import com.nikrasoff.seamlessportals.portals.Portal;
import com.nikrasoff.seamlessportals.portals.PortalManager;
import dev.crmodders.cosmicquilt.api.entrypoint.ModInitializer;
import finalforeach.cosmicreach.GameAssetLoader;
import finalforeach.cosmicreach.blockevents.BlockEvents;
import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.entities.EntityCreator;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.rendering.items.ItemRenderer;
import finalforeach.cosmicreach.util.Identifier;
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

    static String[] blockEventIds = {
            "block_events_portal_destabiliser_off",
            "block_events_portal_destabiliser_on",
            "block_events_portal_generator_off",
            "block_events_portal_generator_on"
    };
    @Override
    public void onInitialize(ModContainer modContainer) {
        LOGGER.info("Initialising Seamless Portals!");

        SeamlessPortalsBlockEvents.registerSeamlessPortalsBlockEvents();

        for (String id: blockIds){
            Block.loadBlock(GameAssetLoader.loadAsset(Identifier.of(MOD_ID, "blocks/" + id + ".json")));
        }
        for (String id: blockEventIds){
            BlockEvents.loadBlockEventsFromAsset(GameAssetLoader.loadAsset(Identifier.of(MOD_ID, "block_events/" + id + ".json")));
        }

        EntityCreator.registerEntityCreator("seamlessportals:entity_portal", Portal::readPortal);
        ItemRenderer.registerItemModelCreator(HandheldPortalGen.class, (handheldPortalGen) -> {
            EntityItemModel newModel = new EntityItemModel("handheld_portal_gen.json", "handheld_portal_gen.anim.json",
                "animation.handheld_portal_generator.idle", "handheld_portal_gen.png");
            newModel.heldModelMatrix.scale(0.5F, 0.5F, 0.5F);
            newModel.heldModelMatrix.translate(0.4F, -0.55F, -1.75F);
            newModel.heldModelMatrix.rotate(Vector3.Y, 175F);
            newModel.heldModelMatrix.translate(-0.25F, -0.25F, -0.25F);
            return newModel;
        });

        Item.registerItem(new HandheldPortalGen());
    }
}
