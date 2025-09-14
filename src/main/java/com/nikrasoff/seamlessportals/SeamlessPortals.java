package com.nikrasoff.seamlessportals;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.nikrasoff.seamlessportals.api.IPortalSolverInitialiser;
import com.nikrasoff.seamlessportals.blockentities.BlockEntityOmniumCalibrator;
import com.nikrasoff.seamlessportals.blockentities.BlockEntityPortalGenerator;
import com.nikrasoff.seamlessportals.blockentities.BlockEntitySpacialAnchor;
import com.nikrasoff.seamlessportals.commands.ClearAnchorsCommand;
import com.nikrasoff.seamlessportals.commands.ListAnchorsCommand;
import com.nikrasoff.seamlessportals.commands.TopCommand;
import com.nikrasoff.seamlessportals.effects.IEffectManager;
import com.nikrasoff.seamlessportals.entities.DestabiliserPulseEntity;
import com.nikrasoff.seamlessportals.networking.packets.*;
import com.nikrasoff.seamlessportals.portals.HPGPortal;
import com.nikrasoff.seamlessportals.portals.Portal;
import com.nikrasoff.seamlessportals.portals.PortalGenPortal;
import com.nikrasoff.seamlessportals.portals.PortalManager;
import dev.puzzleshq.puzzleloader.cosmic.core.modInitialises.ModInit;
import dev.puzzleshq.puzzleloader.cosmic.core.modInitialises.PostModInit;
import dev.puzzleshq.puzzleloader.cosmic.core.modInitialises.PreModInit;
import dev.puzzleshq.puzzleloader.cosmic.game.GameRegistries;
import dev.puzzleshq.puzzleloader.loader.util.PuzzleEntrypointUtil;
import finalforeach.cosmicreach.GameAssetLoader;
import finalforeach.cosmicreach.singletons.GameSingletons;
import finalforeach.cosmicreach.blockentities.BlockEntity;
import finalforeach.cosmicreach.blockevents.BlockEvents;
import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.blocks.BlockStateGenerator;
import finalforeach.cosmicreach.entities.EntityCreator;
import finalforeach.cosmicreach.items.loot.Loot;
import finalforeach.cosmicreach.items.recipes.CraftingRecipes;
import finalforeach.cosmicreach.networking.GamePacket;
import finalforeach.cosmicreach.networking.packets.blockentities.BlockEntityDataPacket;
import finalforeach.cosmicreach.networking.packets.blockentities.BlockEntityScreenPacket;
import finalforeach.cosmicreach.networking.server.ServerIdentity;
import finalforeach.cosmicreach.networking.server.ServerSingletons;
import finalforeach.cosmicreach.util.GameTag;
import finalforeach.cosmicreach.util.Identifier;
import finalforeach.cosmicreach.worldgen.Ore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SeamlessPortals implements PreModInit, ModInit, PostModInit {
    public static PortalManager portalManager = new PortalManager();
    public static IEffectManager effectManager;
    public static ISPClientConstants clientConstants;
    public static Ore oreOmnium;
    public static final Logger LOGGER = LogManager.getLogger(SeamlessPortalsConstants.MOD_ID);

    static String[] blockIds = {
            "portal_generator",
            "portal_destabiliser",
            "omnium_block",
            "omnium_calibrator",
            "spacial_anchor",
            "portal_disruption_block",
            "portal_conductor_block",
            "ore_omnium"
    };

    static String[] recipeIds = {
            "crafting/omnium_block",
            "crafting/portal_conductor",
            "crafting/portal_repulsor",
            "crafting/portal_gen",
            "crafting/warp_core",
            "crafting/hpg",
            "crafting/spacial_anchor",
            "crafting/omnium_calibrator",
            "crafting/portal_destabiliser"
    };

    static String[] blockEventIds = {
            "block_events_portal_destabiliser_off",
            "block_events_portal_destabiliser_on",
            "block_events_portal_generator_off",
            "block_events_portal_generator_on",
            "omnium_calibrator_on",
            "omnium_calibrator_off",
            "spacial_anchor",
            "block_events_ore_omnium"
    };



    @Override
    public void onInit() {
        LOGGER.info("Initialising Seamless Portals!");
        GamePacket.registerPacket(HpgFiredPacket.class);
        GamePacket.registerPacket(PortalAnimationPacket.class);
        GamePacket.registerPacket(PortalClearPacket.class);
        GamePacket.registerPacket(CreateEffectPacket.class);
        GamePacket.registerPacket(UpdatePortalPacket.class);
        GamePacket.registerPacket(PortalDeletePacket.class);
        GamePacket.registerPacket(PortalGeneratorUpdatePacket.class);
        GamePacket.registerPacket(ActivatePortalGenPacket.class);
        GamePacket.registerPacket(DeactivatePortalGenPacket.class);
        GamePacket.registerPacket(ConvergenceEventPacket.class);

        if (!GameSingletons.isClient){
            GameSingletons.registerBlockEntityScreenOpener("seamlessportals:omnium_calibrator", (info) -> {
                ServerIdentity id = ServerSingletons.getConnection(info.player());
                BlockEntity blockEntity = info.blockEntity();
                id.send(new BlockEntityDataPacket(blockEntity));
                id.send(new BlockEntityScreenPacket(blockEntity));
            });
            GameSingletons.registerBlockEntityScreenOpener("seamlessportals:spacial_anchor", (info) -> {
                ServerIdentity id = ServerSingletons.getConnection(info.player());
                BlockEntity blockEntity = info.blockEntity();
                id.send(new BlockEntityDataPacket(blockEntity));
                id.send(new BlockEntityScreenPacket(blockEntity));
            });
            GameSingletons.registerBlockEntityScreenOpener("seamlessportals:portal_generator", (info) -> {
                ServerIdentity id = ServerSingletons.getConnection(info.player());
                BlockEntity blockEntity = info.blockEntity();
                id.send(new BlockEntityDataPacket(blockEntity));
                id.send(new BlockEntityScreenPacket(blockEntity));
            });
        }


        TopCommand.register();
        SeamlessPortalsItems.registerItems();
    }

    @Override
    public void onPostInit() {
        EntityCreator.registerEntityCreator("seamlessportals:entity_portal", Portal::readPortal);
        EntityCreator.registerEntityCreator("seamlessportals:entity_portal_gen_portal", PortalGenPortal::readPortal);
        EntityCreator.registerEntityCreator("seamlessportals:entity_hpg_portal", HPGPortal::readPortal);
        EntityCreator.registerEntityCreator(DestabiliserPulseEntity.ENTITY_ID.toString(), DestabiliserPulseEntity::new);

        Json json = new Json();
        json.setSerializer(Vector3.class, new Json.Serializer<>() {
            public void write(Json json, Vector3 object, Class knownType) {
                json.writeValue(new float[]{object.x, object.y, object.z});
            }

            public Vector3 read(Json json, JsonValue jsonData, Class type) {
                float[] f = jsonData.asFloatArray();
                return new Vector3(f);
            }
        });
        for (String id: blockEventIds){
            BlockEvents.loadBlockEventsFromAsset(json, GameAssetLoader.loadAsset(Identifier.of(SeamlessPortalsConstants.MOD_ID, "block_events/" + id + ".json")));
        }

        BlockStateGenerator.loadGeneratorsFromFile(GameAssetLoader.loadAsset(Identifier.of(SeamlessPortalsConstants.MOD_ID, "block_state_generators/directional_blocks.json")));
        for (String id: blockIds){
            Block.loadBlock(GameAssetLoader.loadAsset(Identifier.of(SeamlessPortalsConstants.MOD_ID, "blocks/" + id + ".json")));
        }
        Loot.loadLoot(GameAssetLoader.loadJson("seamlessportals:loot/ore_omnium.json"));
        json = new Json();
        for (String id : recipeIds){
            CraftingRecipes.loadRecipe(Identifier.of(SeamlessPortalsConstants.MOD_ID, id), json, GameAssetLoader.loadJson(GameAssetLoader.loadAsset(Identifier.of(SeamlessPortalsConstants.MOD_ID, "recipes/" + id + ".json"))));
        }
        BlockEntityOmniumCalibrator.registerBlockEntityCreator();
        BlockEntitySpacialAnchor.registerBlockEntityCreator();
        BlockEntityPortalGenerator.registerBlockEntityCreator();
        oreOmnium = (new Ore(Block.getById(Identifier.of(SeamlessPortalsConstants.MOD_ID, "ore_omnium")).getDefaultBlockState(), GameTag.get("ore_replaceable"))).setMaxElevation(-32).setMaxOresPerCluster(5).setAttemptsPerColumn(3);
        PuzzleEntrypointUtil.invoke("portalInteractionSolver", IPortalSolverInitialiser.class, IPortalSolverInitialiser::initPortalInteractionSolvers);
    }

    @Override
    public void onPreInit() {
        SeamlessPortalsBlockEvents.registerSeamlessPortalsBlockEvents();
    }
}
