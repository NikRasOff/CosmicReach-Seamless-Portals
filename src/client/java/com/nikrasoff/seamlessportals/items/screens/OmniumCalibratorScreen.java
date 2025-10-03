package com.nikrasoff.seamlessportals.items.screens;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.nikrasoff.seamlessportals.SPClientConstants;
import com.nikrasoff.seamlessportals.SeamlessPortalsItems;
import com.nikrasoff.seamlessportals.blocks.blockentities.BlockEntityOmniumCalibrator;
import com.nikrasoff.seamlessportals.items.containers.OmniumCalibratorSlotContainer;
import com.nikrasoff.seamlessportals.ui.widgets.DoubleProgressTexture;
import com.nikrasoff.seamlessportals.ui.widgets.FakeItemSlotWidget;
import com.nikrasoff.seamlessportals.ui.widgets.TextureSwitchWidget;
import dev.puzzleshq.puzzleloader.loader.util.ReflectionUtil;
import finalforeach.cosmicreach.items.ItemSlot;
import finalforeach.cosmicreach.ui.GameStyles;
import finalforeach.cosmicreach.ui.screens.BaseItemScreen;
import finalforeach.cosmicreach.ui.widgets.ContainerSlotWidget;
import io.github.puzzle.cosmic.util.annotation.Note;

import java.lang.reflect.InvocationTargetException;

public class OmniumCalibratorScreen extends BaseItemScreen {
    private final ContainerSlotWidget[] slotWidgets;
    BlockEntityOmniumCalibrator omniumCalibrator;

    public OmniumCalibratorScreen(int windowId, BlockEntityOmniumCalibrator omniumCalibrator) {
        super(windowId, omniumCalibrator);
        this.omniumCalibrator = omniumCalibrator;
        OmniumCalibratorSlotContainer container = omniumCalibrator.slotContainer;
        Stack stack = new Stack();
        Actor background = new Image(GameStyles.containerBackground9Patch);
        Table functionalTable = new Table();
        this.slotWidgets = new ContainerSlotWidget[container.numberOfSlots - 1];
        setSlotWidgets(slotWidgets);

        ItemSlot s = container.getInputSlot();
        ContainerSlotWidget w = new ContainerSlotWidget(windowId, omniumCalibrator, container, s.getSlotId(), s.isOutputOnly());
        this.slotWidgets[0] = w;

        s = container.getOutputSlot1();
        FakeItemSlotWidget w1 = new FakeItemSlotWidget(windowId, omniumCalibrator, container, s.getSlotId(), s.isOutputOnly());
        w1.setFakeItem(SeamlessPortalsItems.OMNIUM_CRYSTAL);
        w1.addAction(new Action() {
            @Override
            public boolean act(float delta) {
                w1.fakeItemVisible = omniumCalibrator.slotContainer.isProcessGoing();
                return false;
            }
        });
        this.slotWidgets[1] = w1;

        s = container.getOutputSlot2();
        @Note("This is widget is not recording the output correctly from what I see.")
        FakeItemSlotWidget w2 = new FakeItemSlotWidget(windowId, omniumCalibrator, container, s.getSlotId(), s.isOutputOnly());
        w2.setFakeItem(SeamlessPortalsItems.OMNIUM_CRYSTAL);
        w2.addAction(new Action() {
            @Override
            public boolean act(float delta) {
                w2.fakeItemVisible = omniumCalibrator.slotContainer.isProcessGoing();
                return false;
            }
        });
        this.slotWidgets[2] = w2;

        functionalTable.add(this.slotWidgets[1]);
        final DoubleProgressTexture progressArrow = new DoubleProgressTexture(SPClientConstants.UI_LASER_WHOLE_OFF.get(), SPClientConstants.UI_LASER_WHOLE_ON.get(), DoubleProgressTexture.Orientation.HORIZONTAL);
        progressArrow.addAction(new Action() {
            public boolean act(float delta) {
                progressArrow.setProgress(omniumCalibrator.getProgressRatio());
                return false;
            }
        });
        functionalTable.add(progressArrow).width(96);
        functionalTable.add(this.slotWidgets[2]);
        functionalTable.row();
        functionalTable.add().height(32);
        functionalTable.row();
        functionalTable.add();
        functionalTable.add(this.slotWidgets[0]);
        functionalTable.setWidth(128.0F);
        functionalTable.setHeight(128.0F);
        background.setWidth(functionalTable.getWidth() + 8.0F);
        background.setHeight(functionalTable.getHeight() + 8.0F);

        Table decorationTable = new Table();

        TextureSwitchWidget laserRight = new TextureSwitchWidget(SPClientConstants.UI_LASER_RIGHT_OFF.get(), SPClientConstants.UI_LASER_RIGHT_ON.get());
        laserRight.addAction(new Action() {
            @Override
            public boolean act(float v) {
                laserRight.setCurrentTexture(omniumCalibrator.slotContainer.isProcessGoing() ? 1 : 0);
                return false;
            }
        });

        decorationTable.add(laserRight).width(32);
        decorationTable.add().width(160).height(32);

        TextureSwitchWidget laserLeft = new TextureSwitchWidget(SPClientConstants.UI_LASER_LEFT_OFF.get(), SPClientConstants.UI_LASER_LEFT_ON.get());
        laserLeft.addAction(new Action() {
            @Override
            public boolean act(float v) {
                laserLeft.setCurrentTexture(omniumCalibrator.slotContainer.isProcessGoing() ? 1 : 0);
                return false;
            }
        });

        decorationTable.add(laserLeft).width(32);
        decorationTable.row();
        decorationTable.add().height(32);
        decorationTable.add(new Image(SPClientConstants.UI_ARROW_OMNIUM_CALIBRATOR.get()));
        decorationTable.row();
        decorationTable.add().height(32);

        stack.add(background);
        stack.add(functionalTable);
        stack.add(decorationTable);
        stack.setBounds(background.getX(), background.getY(), background.getWidth(), background.getHeight());
        this.mainActor = stack;
//        this.slotActor = stack;
        stack.setHeight(stack.getHeight() + 16.0F);

        this.init();
    }

    @Override
    public void onShow() {
        super.onShow();
        for (ContainerSlotWidget slotWidget : slotWidgets) {
            try {
                if (slotWidget instanceof FakeItemSlotWidget slotWidget1) {
                    ReflectionUtil.getMethod(Actor.class, "setStage", Stage.class)
                            .invoke(slotWidget1.itemStackWidget, getActor().getStage());
                }
                ReflectionUtil.getMethod(Actor.class, "setStage", Stage.class)
                        .invoke(slotWidget, getActor().getStage());
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
