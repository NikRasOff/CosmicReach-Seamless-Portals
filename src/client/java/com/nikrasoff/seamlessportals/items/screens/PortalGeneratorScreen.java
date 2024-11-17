package com.nikrasoff.seamlessportals.items.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.github.puzzle.game.ui.font.CosmicReachFont;
import com.nikrasoff.seamlessportals.SPClientConstants;
import com.nikrasoff.seamlessportals.SeamlessPortalsConstants;
import com.nikrasoff.seamlessportals.blockentities.BlockEntityPortalGenerator;
import com.nikrasoff.seamlessportals.items.containers.PortalGeneratorSlotContainer;
import finalforeach.cosmicreach.items.ItemSlot;
import finalforeach.cosmicreach.items.screens.BaseItemScreen;
import finalforeach.cosmicreach.ui.UI;
import finalforeach.cosmicreach.ui.widgets.ItemSlotWidget;

public class PortalGeneratorScreen extends BaseItemScreen {
    BlockEntityPortalGenerator portalGenerator;

    private static final BitmapFont font = CosmicReachFont.createCosmicReachFont();
    private static final Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);
    private static final TextField.TextFieldStyle fieldStyle = new TextField.TextFieldStyle(font, Color.WHITE, new TextureRegionDrawable(SPClientConstants.UI_TEXT_CURSOR), null, new NinePatchDrawable(UI.containerBackground9Patch));
    private static final TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle(new NinePatchDrawable(UI.container9Patch), new NinePatchDrawable(UI.container9PatchHovered), new NinePatchDrawable(UI.containerSelected9Patch), font);

    public PortalGeneratorScreen(BlockEntityPortalGenerator portalGenerator) {
        super(portalGenerator);
        this.portalGenerator = portalGenerator;
        PortalGeneratorSlotContainer container = portalGenerator.slotContainer;
        Stack stack = new Stack();
        Actor background = new Image(UI.containerBackground9Patch);
        this.slotWidgets = new ItemSlotWidget[container.numberOfSlots];

        Table backgroundTable = new Table();
        backgroundTable.add(background).minSize(450, 256);
        Table overallTable = new Table();

        Label sizeLabel = new Label("Portal size", labelStyle);
        overallTable.add(sizeLabel);
        overallTable.row();

        Table higherTable = new Table();
        Label sizeXLabel = new Label("X", labelStyle);
        sizeXLabel.setAlignment(Align.center);
        TextField sizeXField = new TextField("3", fieldStyle);
        sizeXField.setHeight(32);
        sizeXField.setAlignment(Align.center);
        Label sizeYLabel = new Label("Y", labelStyle);
        sizeYLabel.setAlignment(Align.center);
        TextField sizeYField = new TextField("3", fieldStyle);
        sizeYField.setAlignment(Align.center);

        higherTable.add(sizeXLabel).height(32).width(64);
        higherTable.add(sizeYLabel).height(32).width(64);
        higherTable.row();
        higherTable.add(sizeXField).height(32).width(64);
        higherTable.add(sizeYField).height(32).width(64);

        overallTable.add(higherTable);
        overallTable.row();

        Table lowerTable = new Table();
        Label primaryOffsetLabel = new Label("Primary portal offset", labelStyle);
        primaryOffsetLabel.setAlignment(Align.bottom);
        lowerTable.add(primaryOffsetLabel).width(100).bottom().padBottom(7);
        lowerTable.add().height(32);
        Label secondaryOffsetLabel = new Label("Secondary portal offset", labelStyle);
        secondaryOffsetLabel.setAlignment(Align.bottom);
        lowerTable.add(secondaryOffsetLabel).width(100).bottom().padBottom(7);
        lowerTable.row();

        Table primaryOffsetTable = new Table();
        TextField primaryOffsetXField = new TextField("0", fieldStyle);
        primaryOffsetXField.setAlignment(Align.center);
        Label primaryOffsetXLabel = new Label("X", labelStyle);
        primaryOffsetXLabel.setAlignment(Align.center);

        Label primaryOffsetYLabel = new Label("Y", labelStyle);
        primaryOffsetYLabel.setAlignment(Align.center);
        TextField primaryOffsetYField = new TextField("0", fieldStyle);
        primaryOffsetYField.setAlignment(Align.center);

        primaryOffsetTable.add(primaryOffsetXLabel).height(32).width(64);
        primaryOffsetTable.add(primaryOffsetYLabel).height(32).width(64);
        primaryOffsetTable.row();
        primaryOffsetTable.add(primaryOffsetXField).height(32).width(64);
        primaryOffsetTable.add(primaryOffsetYField).height(32).width(64);

        lowerTable.add(primaryOffsetTable).height(64);

        ItemSlot s = container.getInputSlot();
        ItemSlotWidget w = new ItemSlotWidget(portalGenerator, container, s.getSlotId(), s.isOutputOnly());
        this.slotWidgets[0] = w;

        lowerTable.add(this.slotWidgets[0]);

        Table secondaryOffsetTable = new Table();

        Label secondaryOffsetXLabel = new Label("X", labelStyle);
        secondaryOffsetXLabel.setAlignment(Align.center);
        TextField secondaryOffsetXField = new TextField("0", fieldStyle);
        secondaryOffsetXField.setAlignment(Align.center);

        Label secondaryOffsetYLabel = new Label("Y", labelStyle);
        secondaryOffsetYLabel.setAlignment(Align.center);
        TextField secondaryOffsetYField = new TextField("0", fieldStyle);
        secondaryOffsetYField.setAlignment(Align.center);

        secondaryOffsetTable.add(secondaryOffsetXLabel).height(32).width(64);
        secondaryOffsetTable.add(secondaryOffsetYLabel).height(32).width(64);
        secondaryOffsetTable.row();
        secondaryOffsetTable.add(secondaryOffsetXField).height(32).width(64);
        secondaryOffsetTable.add(secondaryOffsetYField).height(32).width(64);

        lowerTable.add(secondaryOffsetTable).height(64);
        lowerTable.row();
        lowerTable.add().height(10);
        lowerTable.row();

        lowerTable.add(new Image(SPClientConstants.UI_PORTAL_GEN_ICON));
        TextButton openPortalButton = new TextButton("Open portal", buttonStyle);
        openPortalButton.getLabel().setAlignment(Align.bottom);
        openPortalButton.getLabelCell().padBottom(5);
        lowerTable.add(openPortalButton);
        lowerTable.add(new Image(SPClientConstants.UI_SPACIAL_ANCHOR_ICON));
        overallTable.add(lowerTable);
        overallTable.row();

        background.setWidth(256F);
        background.setHeight(256F);

        stack.add(backgroundTable);
        stack.add(overallTable);
        stack.setBounds(background.getX(), background.getY(), background.getWidth(), background.getHeight());
        this.slotActor = stack;
        stack.setHeight(stack.getHeight() + 16.0F);
        this.init();
    }
}
