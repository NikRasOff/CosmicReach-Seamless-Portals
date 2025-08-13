package com.nikrasoff.seamlessportals.items.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.nikrasoff.seamlessportals.blockentities.BlockEntitySpacialAnchor;
import com.nikrasoff.seamlessportals.items.containers.SpacialAnchorSlotContainer;
import finalforeach.cosmicreach.items.ItemSlot;
import finalforeach.cosmicreach.items.screens.BaseItemScreen;
import finalforeach.cosmicreach.ui.GameStyles;
import finalforeach.cosmicreach.ui.widgets.ContainerSlotWidget;

public class SpacialAnchorScreen extends BaseItemScreen {
    BlockEntitySpacialAnchor spacialAnchor;

    public SpacialAnchorScreen(int windowId, BlockEntitySpacialAnchor spacialAnchor) {
        super(windowId, spacialAnchor);
        this.spacialAnchor = spacialAnchor;
        SpacialAnchorSlotContainer container = spacialAnchor.slotContainer;
        Stack stack = new Stack();
        Actor background = new Image(GameStyles.containerBackground9Patch);
        Table functionalTable = new Table();
        this.slotWidgets = new ContainerSlotWidget[container.numberOfSlots];

        ItemSlot s = container.getInputSlot();
        ContainerSlotWidget w = new ContainerSlotWidget(windowId, spacialAnchor, container, s.getSlotId(), s.isOutputOnly());
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
