package com.nikrasoff.seamlessportals;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.nikrasoff.seamlessportals.blockentities.BlockEntityOmniumCalibrator;
import com.nikrasoff.seamlessportals.blockentities.BlockEntityPortalGenerator;
import com.nikrasoff.seamlessportals.blockentities.BlockEntitySpacialAnchor;
import com.nikrasoff.seamlessportals.effects.EffectManager;
import com.nikrasoff.seamlessportals.effects.PulseEffect;
import com.nikrasoff.seamlessportals.items.HandheldPortalGen;
import com.nikrasoff.seamlessportals.items.UnstableHandheldPortalGen;
import com.nikrasoff.seamlessportals.items.screens.OmniumCalibratorScreen;
import com.nikrasoff.seamlessportals.items.screens.PortalGeneratorScreen;
import com.nikrasoff.seamlessportals.items.screens.SpacialAnchorScreen;
import com.nikrasoff.seamlessportals.rendering.SeamlessPortalsRenderUtil;
import com.nikrasoff.seamlessportals.rendering.models.ObjItemModel;
import com.nikrasoff.seamlessportals.rendering.models.PortalModel;
import com.nikrasoff.seamlessportals.api.IPortalEntityRendererInitialiser;
import dev.puzzleshq.puzzleloader.cosmic.core.modInitialises.ClientModInit;
import dev.puzzleshq.puzzleloader.loader.util.PuzzleEntrypointUtil;
import finalforeach.cosmicreach.ClientSingletons;
import finalforeach.cosmicreach.singletons.GameSingletons;
import finalforeach.cosmicreach.Threads;
import finalforeach.cosmicreach.rendering.items.ItemRenderer;
import finalforeach.cosmicreach.ui.UI;
import finalforeach.cosmicreach.util.Identifier;

public class SeamlessPortalsClient implements ClientModInit {
    @Override
    public void onClientInit() {
        SeamlessPortals.effectManager = new EffectManager();
        SeamlessPortals.effectManager.registerEffects();
        SeamlessPortals.clientConstants = new SPClientConstants();
        GameSingletons.registerBlockEntityScreenOpener("seamlessportals:omnium_calibrator", (info) -> {
            BlockEntityOmniumCalibrator omniumCalibrator = (BlockEntityOmniumCalibrator) info.blockEntity();
            final OmniumCalibratorScreen screen = new OmniumCalibratorScreen(info.windowId(), (BlockEntityOmniumCalibrator) info.blockEntity());
            UI.addOpenBaseScreen(omniumCalibrator.slotContainer, screen);
            screen.getActor().addAction(new Action() {
                public boolean act(float delta) {
                    if (!omniumCalibrator.loaded) {
                        screen.closeRequested = true;
                        return true;
                    } else {
                        return false;
                    }
                }
            });
        });
        GameSingletons.registerBlockEntityScreenOpener("seamlessportals:spacial_anchor", (info) -> {
            BlockEntitySpacialAnchor spacialAnchor = (BlockEntitySpacialAnchor) info.blockEntity();
            final SpacialAnchorScreen screen = new SpacialAnchorScreen(info.windowId(), (BlockEntitySpacialAnchor) info.blockEntity());
            UI.addOpenBaseScreen(spacialAnchor.slotContainer, screen);
            screen.getActor().addAction(new Action() {
                public boolean act(float delta) {
                    if (!spacialAnchor.loaded) {
                        screen.closeRequested = true;
                        return true;
                    } else {
                        return false;
                    }
                }
            });
        });
        GameSingletons.registerBlockEntityScreenOpener("seamlessportals:portal_generator", (info) -> {
            BlockEntityPortalGenerator portalGenerator = (BlockEntityPortalGenerator) info.blockEntity();
            final PortalGeneratorScreen screen = new PortalGeneratorScreen(info.windowId(), (BlockEntityPortalGenerator) info.blockEntity());
            UI.addOpenBaseScreen(portalGenerator.slotContainer, screen);
            screen.getActor().addAction(new Action() {
                public boolean act(float delta) {
                    if (!portalGenerator.loaded) {
                        screen.closeRequested = true;
                        return true;
                    } else {
                        return false;
                    }
                }
            });
        });
        Threads.runOnMainThread(SeamlessPortalsClient::renderInit);
    }

    public static void renderInit(){
        SPClientConstants.init();
        PuzzleEntrypointUtil.invoke("portalEntityRender", IPortalEntityRendererInitialiser.class, IPortalEntityRendererInitialiser::initPortalEntityRenderers);
        SeamlessPortalsRenderUtil.initialise();
        PulseEffect.create();
        PortalModel.create();
        PortalGeneratorScreen.initialise();

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

        SeamlessPortalsRenderUtil.loadModel(Identifier.of(SeamlessPortalsConstants.MOD_ID, "models/view/unstable_hpg.g3db"));
        ItemRenderer.registerItemModelCreator(UnstableHandheldPortalGen.class, (handheldPortalGen) -> {
            ObjItemModel newModel = new ObjItemModel(Identifier.of(SeamlessPortalsConstants.MOD_ID, "models/view/unstable_hpg.g3db"));
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
    }
}
