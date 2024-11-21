package com.nikrasoff.seamlessportals;

import com.github.puzzle.access_manipulators.AccessManipulators;
import com.github.puzzle.core.loader.provider.mod.entrypoint.impls.ModInitializer;
import com.github.puzzle.core.loader.provider.mod.entrypoint.impls.PostModInitializer;
import com.github.puzzle.core.loader.util.ModLocator;
import com.github.puzzle.game.PuzzleRegistries;
import com.github.puzzle.game.block.DataModBlock;
import com.github.puzzle.game.events.OnRegisterBlockEvent;
import com.nikrasoff.seamlessportals.blockentities.BlockEntityOmniumCalibrator;
import com.nikrasoff.seamlessportals.blockentities.BlockEntityPortalGenerator;
import com.nikrasoff.seamlessportals.blockentities.BlockEntitySpacialAnchor;
import com.nikrasoff.seamlessportals.effects.IEffectManager;
import com.nikrasoff.seamlessportals.entities.DestabiliserPulseEntity;
import com.nikrasoff.seamlessportals.networking.packets.CreateEffectPacket;
import com.nikrasoff.seamlessportals.networking.packets.HpgFiredPacket;
import com.nikrasoff.seamlessportals.networking.packets.PortalAnimationPacket;
import com.nikrasoff.seamlessportals.networking.packets.PortalClearPacket;
import com.nikrasoff.seamlessportals.portals.Portal;
import com.nikrasoff.seamlessportals.portals.PortalManager;
import finalforeach.cosmicreach.GameAssetLoader;
import finalforeach.cosmicreach.blockevents.BlockEvents;
import finalforeach.cosmicreach.blocks.BlockStateGenerator;
import finalforeach.cosmicreach.entities.EntityCreator;
import finalforeach.cosmicreach.items.recipes.CraftingRecipes;
import finalforeach.cosmicreach.networking.GamePacket;
import finalforeach.cosmicreach.util.Identifier;
import meteordevelopment.orbit.EventHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SeamlessPortals implements ModInitializer, PostModInitializer {
    public static boolean debugOutlines = true;
    public static PortalManager portalManager = new PortalManager();
    public static IEffectManager effectManager;
    public static ISPClientConstants clientConstants;
    public static final Logger LOGGER = LogManager.getLogger(SeamlessPortalsConstants.MOD_ID);

    static String[] blockIds = {
            "portal_generator",
            "portal_destabiliser",
            "omnium_block",
            "omnium_calibrator",
            "spacial_anchor"
    };

    static String[] recipeIds = {
            "crafting/omnium_block"
    };

    static String[] blockEventIds = {
            "block_events_portal_destabiliser_off",
            "block_events_portal_destabiliser_on",
            "block_events_portal_generator_off",
            "block_events_portal_generator_on",
            "omnium_calibrator_on",
            "omnium_calibrator_off",
            "spacial_anchor"
    };

    @Override
    public void onInit() {
        PuzzleRegistries.EVENT_BUS.subscribe(this);
        LOGGER.info("Initialising Seamless Portals!");
        GamePacket.registerPacket(HpgFiredPacket.class);
        GamePacket.registerPacket(PortalAnimationPacket.class);
        GamePacket.registerPacket(PortalClearPacket.class);
        GamePacket.registerPacket(CreateEffectPacket.class);

        SeamlessPortalsBlockEvents.registerSeamlessPortalsBlockEvents();
        EntityCreator.registerEntityCreator("seamlessportals:entity_portal", Portal::readPortal);
        EntityCreator.registerEntityCreator(DestabiliserPulseEntity.ENTITY_ID.toString(), DestabiliserPulseEntity::new);
    }

    @EventHandler
    public void onEvent(OnRegisterBlockEvent event){
        BlockStateGenerator.loadGeneratorsFromFile(GameAssetLoader.loadAsset(Identifier.of(SeamlessPortalsConstants.MOD_ID, "block_state_generators/directional_blocks.json")));
        for (String id: blockIds){
            event.registerBlock(() -> new DataModBlock(Identifier.of(SeamlessPortalsConstants.MOD_ID, id + ".json")));
        }
        for (String id: blockEventIds){
            BlockEvents.loadBlockEventsFromAsset(GameAssetLoader.loadAsset(Identifier.of(SeamlessPortalsConstants.MOD_ID, "block_events/" + id + ".json")));
        }
    }

    @Override
    public void onPostInit() {
        // Yet again more post-post-inits because it do be like this
        SeamlessPortalsItems.registerItems();
        for (String id : recipeIds){
            CraftingRecipes.loadRecipe(GameAssetLoader.loadJson(GameAssetLoader.loadAsset(Identifier.of(SeamlessPortalsConstants.MOD_ID, "recipes/" + id + ".json"))));
        }
        BlockEntityOmniumCalibrator.registerBlockEntityCreator();
        BlockEntitySpacialAnchor.registerBlockEntityCreator();
        BlockEntityPortalGenerator.registerBlockEntityCreator();
    }
}
