package com.nikrasoff.seamlessportals.items.screens;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.nikrasoff.seamlessportals.blockentities.BlockEntityOmniumCalibrator;
import com.nikrasoff.seamlessportals.items.containers.OmniumCalibratorSlotContainer;
import finalforeach.cosmicreach.items.ItemSlot;
import finalforeach.cosmicreach.items.screens.BaseItemScreen;
import finalforeach.cosmicreach.ui.UI;
import finalforeach.cosmicreach.ui.widgets.ItemSlotWidget;
import finalforeach.cosmicreach.ui.widgets.ProgressArrowTexture;
import finalforeach.cosmicreach.util.Orientation2D;

public class OmniumCalibratorScreen extends BaseItemScreen {
    BlockEntityOmniumCalibrator omniumCalibrator;

    public OmniumCalibratorScreen(final BlockEntityOmniumCalibrator omniumCalibrator) {
        OmniumCalibratorSlotContainer container = omniumCalibrator.slotContainer;
        Stack stack = new Stack();
        Actor background = new Image(UI.containerBackground9Patch);
        Table table = new Table();
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

        table.add(this.slotWidgets[1]);
        final ProgressArrowTexture progressArrow = new ProgressArrowTexture(UI.progressArrowEmptyTex, UI.progressArrowFullTex, Orientation2D.RIGHT);
        progressArrow.addAction(new Action() {
            public boolean act(float delta) {
                progressArrow.setProgress(omniumCalibrator.getProgressRatio());
                return false;
            }
        });
        table.add(progressArrow);
        table.add(this.slotWidgets[2]);
        table.row();
        table.add(this.slotWidgets[0]);
        table.setWidth(128.0F);
        table.setHeight(128.0F);
        background.setWidth(table.getWidth() + 8.0F);
        background.setHeight(table.getHeight() + 8.0F);
        stack.add(background);
        stack.add(table);
        stack.setBounds(background.getX(), background.getY(), background.getWidth(), background.getHeight());
        this.slotActor = stack;
        stack.setHeight(stack.getHeight() + 16.0F);
        this.init();
    }
}
