package com.nikrasoff.seamlessportals;

import com.badlogic.gdx.utils.Array;
import com.github.puzzle.core.loader.provider.mod.entrypoint.impls.ModInitializer;
import com.github.puzzle.core.loader.provider.mod.entrypoint.impls.PostModInitializer;
import com.github.puzzle.game.PuzzleRegistries;
import com.github.puzzle.game.block.DataModBlock;
import com.github.puzzle.game.commands.CommandManager;
import com.github.puzzle.game.commands.CommandSource;
import com.github.puzzle.game.commands.ServerCommandSource;
import com.github.puzzle.game.events.OnRegisterBlockEvent;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.nikrasoff.seamlessportals.blockentities.BlockEntityOmniumCalibrator;
import com.nikrasoff.seamlessportals.blockentities.BlockEntityPortalGenerator;
import com.nikrasoff.seamlessportals.blockentities.BlockEntitySpacialAnchor;
import com.nikrasoff.seamlessportals.commands.ClearAnchorsCommand;
import com.nikrasoff.seamlessportals.commands.ListAnchorsCommand;
import com.nikrasoff.seamlessportals.effects.IEffectManager;
import com.nikrasoff.seamlessportals.entities.DestabiliserPulseEntity;
import com.nikrasoff.seamlessportals.extras.PortalSpawnBlockInfo;
import com.nikrasoff.seamlessportals.networking.packets.*;
import com.nikrasoff.seamlessportals.portals.HPGPortal;
import com.nikrasoff.seamlessportals.portals.Portal;
import com.nikrasoff.seamlessportals.portals.PortalGenPortal;
import com.nikrasoff.seamlessportals.portals.PortalManager;
import finalforeach.cosmicreach.GameAssetLoader;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.blockentities.BlockEntity;
import finalforeach.cosmicreach.blockevents.BlockEvents;
import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.blocks.BlockStateGenerator;
import finalforeach.cosmicreach.chat.Chat;
import finalforeach.cosmicreach.entities.EntityCreator;
import finalforeach.cosmicreach.items.loot.Loot;
import finalforeach.cosmicreach.items.recipes.CraftingRecipes;
import finalforeach.cosmicreach.networking.GamePacket;
import finalforeach.cosmicreach.networking.packets.blocks.BlockEntityDataPacket;
import finalforeach.cosmicreach.networking.packets.blocks.BlockEntityScreenPacket;
import finalforeach.cosmicreach.networking.server.ServerIdentity;
import finalforeach.cosmicreach.networking.server.ServerSingletons;
import finalforeach.cosmicreach.util.Identifier;
import finalforeach.cosmicreach.worldgen.Ore;
import meteordevelopment.orbit.EventHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.injection.struct.InjectorGroupInfo;

import java.util.Map;
import java.util.function.Predicate;

public class SeamlessPortals implements ModInitializer, PostModInitializer {
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
            "crafting/laser_emitter",
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
        PuzzleRegistries.EVENT_BUS.subscribe(this);
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

        SeamlessPortalsBlockEvents.registerSeamlessPortalsBlockEvents();
        EntityCreator.registerEntityCreator("seamlessportals:entity_portal", Portal::readPortal);
        EntityCreator.registerEntityCreator("seamlessportals:entity_portal_gen_portal", PortalGenPortal::readPortal);
        EntityCreator.registerEntityCreator("seamlessportals:entity_hpg_portal", HPGPortal::readPortal);
        EntityCreator.registerEntityCreator(DestabiliserPulseEntity.ENTITY_ID.toString(), DestabiliserPulseEntity::new);

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


        ListAnchorsCommand.register(CommandManager.DISPATCHER);
        ClearAnchorsCommand.register(CommandManager.DISPATCHER);
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
        SeamlessPortalsItems.registerItems();
        Loot.loadLoot(GameAssetLoader.loadJson("seamlessportals:loot/ore_omnium.json"));
    }

    @Override
    public void onPostInit() {
        // Yet again more post-post-inits because it do be like this
        for (String id : recipeIds){
            CraftingRecipes.loadRecipe(GameAssetLoader.loadJson(GameAssetLoader.loadAsset(Identifier.of(SeamlessPortalsConstants.MOD_ID, "recipes/" + id + ".json"))));
        }
        BlockEntityOmniumCalibrator.registerBlockEntityCreator();
        BlockEntitySpacialAnchor.registerBlockEntityCreator();
        BlockEntityPortalGenerator.registerBlockEntityCreator();
        oreOmnium = (new Ore(Block.getById(Identifier.of(SeamlessPortalsConstants.MOD_ID, "ore_omnium")).getDefaultBlockState(), "ore_replaceable")).setMaxElevation(-32).setMaxOresPerCluster(5).setAttemptsPerColumn(2);
    }
}
