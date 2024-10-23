package com.nikrasoff.seamlessportals.items.screens;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.nikrasoff.seamlessportals.SeamlessPortalsConstants;
import com.nikrasoff.seamlessportals.blockentities.BlockEntityOmniumCalibrator;
import com.nikrasoff.seamlessportals.items.containers.OmniumCalibratorSlotContainer;
import com.nikrasoff.seamlessportals.ui.widgets.DoubleProgressTexture;
import finalforeach.cosmicreach.items.ItemSlot;
import finalforeach.cosmicreach.items.screens.BaseItemScreen;
import finalforeach.cosmicreach.ui.UI;
import finalforeach.cosmicreach.ui.widgets.ItemSlotWidget;

public class OmniumCalibratorScreen extends BaseItemScreen {
    BlockEntityOmniumCalibrator omniumCalibrator;

    public OmniumCalibratorScreen(final BlockEntityOmniumCalibrator omniumCalibrator) {
        OmniumCalibratorSlotContainer container = omniumCalibrator.slotContainer;
        Stack stack = new Stack();
        Actor background = new Image(UI.containerBackground9Patch);
        Table functionalTable = new Table();
        this.slotWidgets = new ItemSlotWidget[container.numberOfSlots - 1];

        ItemSlot s = container.getInputSlot();
        ItemSlotWidget w = new ItemSlotWidget(container, s, s.isOutputOnly());
        this.slotWidgets[0] = w;

        s = container.getOutputSlot1();
        w = new ItemSlotWidget(container, s, s.isOutputOnly());
        this.slotWidgets[1] = w;

        s = container.getOutputSlot2();
        w = new ItemSlotWidget(container, s, s.isOutputOnly());
        this.slotWidgets[2] = w;

        functionalTable.add(this.slotWidgets[1]);
        final DoubleProgressTexture progressArrow = new DoubleProgressTexture(SeamlessPortalsConstants.UI_LASER_WHOLE_OFF, SeamlessPortalsConstants.UI_LASER_WHOLE_ON, DoubleProgressTexture.Orientation.HORIZONTAL);
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
        decorationTable.add(new Image(SeamlessPortalsConstants.UI_LASER_RIGHT_ON)).width(32);
        decorationTable.add().width(160).height(32);
        decorationTable.add(new Image(SeamlessPortalsConstants.UI_LASER_LEFT_ON)).width(32);
        decorationTable.row();
        decorationTable.add().height(32);
        decorationTable.add(new Image(SeamlessPortalsConstants.UI_ARROW_OMNIUM_CALIBRATOR));
        decorationTable.row();
        decorationTable.add().height(32);

        stack.add(background);
        stack.add(functionalTable);
        stack.add(decorationTable);
        stack.setBounds(background.getX(), background.getY(), background.getWidth(), background.getHeight());
        this.slotActor = stack;
        stack.setHeight(stack.getHeight() + 16.0F);
        this.init();
    }
}
