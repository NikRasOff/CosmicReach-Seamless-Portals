package com.nikrasoff.seamlessportals;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.github.puzzle.core.PuzzleRegistries;
import com.github.puzzle.game.block.DataModBlock;
import com.github.puzzle.game.events.OnRegisterBlockEvent;
import com.github.puzzle.game.items.IModItem;
import com.github.puzzle.loader.entrypoint.interfaces.ModInitializer;
import com.github.puzzle.loader.entrypoint.interfaces.PostModInitializer;
import com.nikrasoff.seamlessportals.blockentities.BlockEntityOmniumCalibrator;
import com.nikrasoff.seamlessportals.effects.PulseEffect;
import com.nikrasoff.seamlessportals.items.HandheldPortalGen;
import com.nikrasoff.seamlessportals.items.SyncedOmniumCrystal;
import com.nikrasoff.seamlessportals.items.screens.OmniumCalibratorScreen;
import com.nikrasoff.seamlessportals.rendering.SeamlessPortalsRenderUtil;
import com.nikrasoff.seamlessportals.rendering.models.ObjItemModel;
import com.nikrasoff.seamlessportals.portals.Portal;
import com.nikrasoff.seamlessportals.portals.PortalManager;
import com.nikrasoff.seamlessportals.rendering.models.PortalModel;
import finalforeach.cosmicreach.GameAssetLoader;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.blockevents.BlockEvents;
import finalforeach.cosmicreach.entities.EntityCreator;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.items.ItemThing;
import finalforeach.cosmicreach.items.recipes.CraftingRecipes;
import finalforeach.cosmicreach.items.screens.FurnaceScreen;
import finalforeach.cosmicreach.rendering.items.ItemRenderer;
import finalforeach.cosmicreach.ui.UI;
import finalforeach.cosmicreach.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.greenrobot.eventbus.Subscribe;

public class SeamlessPortals implements ModInitializer, PostModInitializer {
    public static boolean debugOutlines = true;
    public static PortalManager portalManager = new PortalManager();
    public static final Logger LOGGER = LogManager.getLogger(SeamlessPortalsConstants.MOD_ID);

    static String[] blockIds = {
            "portal_generator",
            "portal_destabiliser",
            "omnium_block",
            "omnium_calibrator"
    };

    static String[] itemIds = {
            "omnium_crystal"
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
            "omnium_calibrator_off"
    };

    @Override
    public void onInit() {
        PuzzleRegistries.EVENT_BUS.register(this);
        LOGGER.info("Initialising Seamless Portals!");

        SeamlessPortalsBlockEvents.registerSeamlessPortalsBlockEvents();
        EntityCreator.registerEntityCreator("seamlessportals:entity_portal", Portal::readPortal);
        for (String id : itemIds){
            ItemThing.loadItemFromJson(GameAssetLoader.loadJson(GameAssetLoader.loadAsset(Identifier.of(SeamlessPortalsConstants.MOD_ID, "items/" + id + ".json"))));
        }
        IModItem.registerItem(new SyncedOmniumCrystal());
    }

    @Subscribe
    public void onEvent(OnRegisterBlockEvent event){
        for (String id: blockIds){
            event.registerBlock(() -> new DataModBlock(Identifier.of(SeamlessPortalsConstants.MOD_ID, id + ".json")));
        }
        for (String id: blockEventIds){
            BlockEvents.loadBlockEventsFromAsset(GameAssetLoader.loadAsset(Identifier.of(SeamlessPortalsConstants.MOD_ID, "block_events/" + id + ".json")));
        }
    }

    public static void extraInit(){
        // No idea why, but this doesn't work without me doing it like this
        SeamlessPortalsConstants.init();
        SeamlessPortalsRenderUtil.initialise();
        PulseEffect.create();
        PortalModel.create();

        SeamlessPortalsRenderUtil.loadModel(Identifier.of(SeamlessPortalsConstants.MOD_ID, "models/view/hpg.g3db"));
        ItemRenderer.registerItemModelCreator(HandheldPortalGen.class, (handheldPortalGen) -> {
            ObjItemModel newModel = new ObjItemModel(Identifier.of(SeamlessPortalsConstants.MOD_ID, "models/view/hpg.g3db"));
            newModel.setAnimation("armature|anim_idle", -1);
            newModel.setViewAnimation("armature|anim_idle", -1);
            newModel.heldModelMatrix.scale(0.5F, 0.5F, 0.5F);
            newModel.heldModelMatrix.translate(0.4F, -0.55F, -1.75F);
            newModel.heldModelMatrix.rotate(Vector3.Y, 175F);
            newModel.heldModelMatrix.translate(-0.25F, -0.25F, -0.25F);

            newModel.onGroundModelMatrix.scale(2, 2, 2);
            newModel.onGroundModelMatrix.translate(0.25f, 0.1f, 0.25f);
            return newModel;
        });

        Item.registerItem(new HandheldPortalGen());
    }

    @Override
    public void onPostInit() {
        // Yet again more post-post-inits because it do be like this
        for (String id : recipeIds){
            CraftingRecipes.loadRecipe(GameAssetLoader.loadJson(GameAssetLoader.loadAsset(Identifier.of(SeamlessPortalsConstants.MOD_ID, "recipes/" + id + ".json"))));
        }
        BlockEntityOmniumCalibrator.registerBlockEntityCreator();
        GameSingletons.blockEntityScreenOpeners.put("seamlessportals:omnium_calibrator", (player, zone, blockEntity) -> {
            BlockEntityOmniumCalibrator omniumCalibrator = (BlockEntityOmniumCalibrator) blockEntity;
            final OmniumCalibratorScreen screen = new OmniumCalibratorScreen((BlockEntityOmniumCalibrator) blockEntity);
            UI.addOpenBaseItemScreen(omniumCalibrator.slotContainer, screen);
            screen.getActor().addAction(new Action() {
                public boolean act(float delta) {
                    if (!blockEntity.loaded) {
                        screen.closeRequested = true;
                        return true;
                    } else {
                        return false;
                    }
                }
            });
        });
    }
}
