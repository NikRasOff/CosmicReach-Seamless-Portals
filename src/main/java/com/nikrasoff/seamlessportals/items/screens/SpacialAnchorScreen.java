package com.nikrasoff.seamlessportals.items.screens;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.nikrasoff.seamlessportals.SeamlessPortalsConstants;
import com.nikrasoff.seamlessportals.SeamlessPortalsItems;
import com.nikrasoff.seamlessportals.blockentities.BlockEntityOmniumCalibrator;
import com.nikrasoff.seamlessportals.blockentities.BlockEntitySpacialAnchor;
import com.nikrasoff.seamlessportals.items.containers.OmniumCalibratorSlotContainer;
import com.nikrasoff.seamlessportals.items.containers.SpacialAnchorSlotContainer;
import com.nikrasoff.seamlessportals.ui.widgets.DoubleProgressTexture;
import com.nikrasoff.seamlessportals.ui.widgets.FakeItemSlotWidget;
import com.nikrasoff.seamlessportals.ui.widgets.TextureSwitchWidget;
import finalforeach.cosmicreach.items.ItemSlot;
import finalforeach.cosmicreach.items.screens.BaseItemScreen;
import finalforeach.cosmicreach.ui.UI;
import finalforeach.cosmicreach.ui.widgets.ItemSlotWidget;

public class SpacialAnchorScreen extends BaseItemScreen {
    BlockEntitySpacialAnchor spacialAnchor;

    public SpacialAnchorScreen(BlockEntitySpacialAnchor spacialAnchor) {
        this.spacialAnchor = spacialAnchor;
        SpacialAnchorSlotContainer container = spacialAnchor.slotContainer;
        Stack stack = new Stack();
        Actor background = new Image(UI.containerBackground9Patch);
        Table functionalTable = new Table();
        this.slotWidgets = new ItemSlotWidget[container.numberOfSlots];

        ItemSlot s = container.getInputSlot();
        ItemSlotWidget w = new ItemSlotWidget(container, s, s.isOutputOnly());
        this.slotWidgets[0] = w;

        functionalTable.add(this.slotWidgets[0]);
        functionalTable.setWidth(128.0F);
        functionalTable.setHeight(128.0F);
        background.setWidth(functionalTable.getWidth() + 8.0F);
        background.setHeight(functionalTable.getHeight() + 8.0F);

        stack.add(background);
        stack.add(functionalTable);
        stack.setBounds(background.getX(), background.getY(), background.getWidth(), background.getHeight());
        this.slotActor = stack;
        stack.setHeight(stack.getHeight() + 16.0F);
        this.init();
    }
}
