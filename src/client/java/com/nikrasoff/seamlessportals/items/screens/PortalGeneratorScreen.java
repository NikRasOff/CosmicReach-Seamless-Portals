package com.nikrasoff.seamlessportals.items.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.github.puzzle.game.ui.font.CosmicReachFont;
import com.nikrasoff.seamlessportals.SPClientConstants;
import com.nikrasoff.seamlessportals.blockentities.BlockEntityPortalGenerator;
import com.nikrasoff.seamlessportals.extras.SomeStringUtils;
import com.nikrasoff.seamlessportals.items.containers.PortalGeneratorSlotContainer;
import com.nikrasoff.seamlessportals.networking.packets.ActivatePortalGenPacket;
import com.nikrasoff.seamlessportals.networking.packets.DeactivatePortalGenPacket;
import com.nikrasoff.seamlessportals.networking.packets.PortalGeneratorUpdatePacket;
import finalforeach.cosmicreach.chat.Chat;
import finalforeach.cosmicreach.items.ItemSlot;
import finalforeach.cosmicreach.items.screens.BaseItemScreen;
import finalforeach.cosmicreach.lang.Lang;
import finalforeach.cosmicreach.networking.client.ClientNetworkManager;
import finalforeach.cosmicreach.networking.packets.blocks.BlockEntityDataPacket;
import finalforeach.cosmicreach.ui.UI;
import finalforeach.cosmicreach.ui.widgets.ItemSlotWidget;

public class PortalGeneratorScreen extends BaseItemScreen {
    BlockEntityPortalGenerator portalGenerator;

    private static BitmapFont font;
    private static Label.LabelStyle labelStyle;
    private static TextField.TextFieldStyle fieldStyle;
    private static TextButton.TextButtonStyle buttonStyle;

    TextField sizeXField;
    TextField sizeYField;
    TextField primaryOffsetXField;
    TextField primaryOffsetYField;
    TextField secondaryOffsetXField;
    TextField secondaryOffsetYField;

    public PortalGeneratorScreen(BlockEntityPortalGenerator portalGenerator) {
        super(portalGenerator);
        if (fieldStyle == null) {
            fieldStyle = new TextField.TextFieldStyle(font, Color.WHITE, new TextureRegionDrawable(SPClientConstants.UI_TEXT_CURSOR), null, new NinePatchDrawable(UI.containerBackground9Patch));
        }
        if (buttonStyle == null) {
            buttonStyle = new TextButton.TextButtonStyle(new NinePatchDrawable(UI.container9Patch), new NinePatchDrawable(UI.container9PatchHovered), new NinePatchDrawable(UI.containerSelected9Patch), font);
        }
        this.portalGenerator = portalGenerator;
        PortalGeneratorSlotContainer container = portalGenerator.slotContainer;
        Stack stack = new Stack();
        Actor background = new Image(UI.containerBackground9Patch);
        this.slotWidgets = new ItemSlotWidget[container.numberOfSlots];

        Table backgroundTable = new Table();
        backgroundTable.add(background).minSize(450, 256);
        Table overallTable = new Table();

        Label sizeLabel = new Label(Lang.get("seamlessportals:portal_size"), labelStyle);
        sizeLabel.setAlignment(Align.center);
        overallTable.add(sizeLabel).height(32).center();
        overallTable.row();

        Table higherTable = new Table();
        Label sizeXLabel = new Label("X", labelStyle);
        sizeXLabel.setAlignment(Align.center);
        sizeXField = new TextField(String.valueOf(portalGenerator.portalSize.x), fieldStyle);
        sizeXField.setAlignment(Align.center);
        sizeXField.setHeight(32);
        Label sizeYLabel = new Label("Y", labelStyle);
        sizeYLabel.setAlignment(Align.center);
        sizeYField = new TextField(String.valueOf(portalGenerator.portalSize.y), fieldStyle);
        sizeYField.setAlignment(Align.center);

        higherTable.add(sizeXLabel).height(32).width(64);
        higherTable.add(sizeYLabel).height(32).width(64);
        higherTable.row();
        higherTable.add(sizeXField).height(32).width(64);
        higherTable.add(sizeYField).height(32).width(64);

        overallTable.add(higherTable);
        overallTable.row();

        Table lowerTable = new Table();
        Label primaryOffsetLabel = new Label(Lang.get("seamlessportals:portal_offset1"), labelStyle);
        primaryOffsetLabel.setAlignment(Align.center);
        lowerTable.add(primaryOffsetLabel).width(100);
        lowerTable.add().height(32);
        Label secondaryOffsetLabel = new Label(Lang.get("seamlessportals:portal_offset2"), labelStyle);
        secondaryOffsetLabel.setAlignment(Align.center);
        lowerTable.add(secondaryOffsetLabel).width(100);
        lowerTable.row();

        Table primaryOffsetTable = new Table();
        primaryOffsetXField = new TextField(String.valueOf(portalGenerator.entrancePortalOffset.x), fieldStyle);
        primaryOffsetXField.addAction(new Action() {
            @Override
            public boolean act(float delta) {
                String text = primaryOffsetXField.getText();
                if (SomeStringUtils.isValidFloat(text) && Float.parseFloat(text) != portalGenerator.entrancePortalOffset.x){
                    portalGenerator.entrancePortalOffset.x = Float.parseFloat(text);
                    if (ClientNetworkManager.isConnected()){
                        ClientNetworkManager.sendAsClient(new BlockEntityDataPacket(portalGenerator));
                    }
                }
                return false;
            }
        });
        primaryOffsetXField.setAlignment(Align.center);
        Label primaryOffsetXLabel = new Label("X", labelStyle);
        primaryOffsetXLabel.setAlignment(Align.center);

        Label primaryOffsetYLabel = new Label("Y", labelStyle);
        primaryOffsetYLabel.setAlignment(Align.center);
        primaryOffsetYField = new TextField(String.valueOf(portalGenerator.entrancePortalOffset.y), fieldStyle);
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
        secondaryOffsetXField = new TextField(String.valueOf(portalGenerator.exitPortalOffset.x), fieldStyle);
        secondaryOffsetXField.setAlignment(Align.center);

        Label secondaryOffsetYLabel = new Label("Y", labelStyle);
        secondaryOffsetYLabel.setAlignment(Align.center);
        secondaryOffsetYField = new TextField(String.valueOf(portalGenerator.exitPortalOffset.y), fieldStyle);
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
        TextButton openPortalButton = new TextButton( portalGenerator.isPortalActive() ? Lang.get("seamlessportals:close_portal") : Lang.get("seamlessportals:open_portal"), buttonStyle);
        openPortalButton.addAction(new Action() {
            @Override
            public boolean act(float v) {
                if (portalGenerator.justUpdated) {
                    portalGenerator.justUpdated = false;
                    openPortalButton.setText(portalGenerator.isPortalActive() ? Lang.get("seamlessportals:close_portal") : Lang.get("seamlessportals:open_portal"));
                }
                if (openPortalButton.isChecked()){
                    openPortalButton.setChecked(false);
                    updatePortalGenerator();
                    if (!portalGenerator.isPortalActive() && portalGenerator.slotContainer.isItemValid()){
                        openPortalButton.setText(Lang.get("seamlessportals:close_portal"));
                        if (ClientNetworkManager.isConnected()){
                            ClientNetworkManager.sendAsClient(new ActivatePortalGenPacket(portalGenerator));
                        }
                        else{
                            portalGenerator.openPortal();
                        }
                    }
                    else{
                        openPortalButton.setText(Lang.get("seamlessportals:open_portal"));
                        if (ClientNetworkManager.isConnected()){
                            ClientNetworkManager.sendAsClient(new DeactivatePortalGenPacket(portalGenerator));
                        }
                        else{
                            portalGenerator.closePortal();
                        }
                    }
                }
                return false;
            }
        });
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

    public static void initialise(){
        font = CosmicReachFont.createCosmicReachFont();
        font.getData().capHeight = -16;
        labelStyle = new Label.LabelStyle(font, Color.WHITE);
    }

    private void yellInChat(String subject){
        Chat.MAIN_CLIENT_CHAT.addMessage(null, "\"" + subject + "\" is not a valid floating point value");
    }

    public void updatePortalGenerator(){
        if (SomeStringUtils.isValidFloat(this.sizeXField.getText())){
            this.portalGenerator.portalSize.x = MathUtils.clamp(Float.parseFloat(this.sizeXField.getText()), 1, 5);
        }
        else this.yellInChat(this.sizeXField.getText());
        if (SomeStringUtils.isValidFloat(this.sizeYField.getText())){
            this.portalGenerator.portalSize.y = MathUtils.clamp(Float.parseFloat(this.sizeYField.getText()), 1, 5);
        }
        else this.yellInChat(this.sizeYField.getText());
        if (SomeStringUtils.isValidFloat(this.primaryOffsetXField.getText())){
            this.portalGenerator.entrancePortalOffset.x = MathUtils.clamp(Float.parseFloat(this.primaryOffsetXField.getText()), -this.portalGenerator.getMaxOffsetX(), this.portalGenerator.getMaxOffsetX());
        }
        else this.yellInChat(this.primaryOffsetXField.getText());
        if (SomeStringUtils.isValidFloat(this.primaryOffsetYField.getText())){
            this.portalGenerator.entrancePortalOffset.y = MathUtils.clamp(Float.parseFloat(this.primaryOffsetYField.getText()), -this.portalGenerator.getMaxOffsetY(), this.portalGenerator.getMaxOffsetY());
        }
        else this.yellInChat(this.primaryOffsetYField.getText());
        if (SomeStringUtils.isValidFloat(this.secondaryOffsetXField.getText())){
            this.portalGenerator.exitPortalOffset.x = MathUtils.clamp(Float.parseFloat(this.secondaryOffsetXField.getText()), -this.portalGenerator.getMaxOffsetX(), this.portalGenerator.getMaxOffsetX());
        }
        else this.yellInChat(this.secondaryOffsetXField.getText());
        if (SomeStringUtils.isValidFloat(this.secondaryOffsetYField.getText())){
            this.portalGenerator.exitPortalOffset.y = MathUtils.clamp(Float.parseFloat(this.secondaryOffsetYField.getText()), -this.portalGenerator.getMaxOffsetY(), this.portalGenerator.getMaxOffsetY());
        }
        else this.yellInChat(this.secondaryOffsetYField.getText());
        if (ClientNetworkManager.isConnected()){
            ClientNetworkManager.sendAsClient(new PortalGeneratorUpdatePacket(this.portalGenerator));
        }
    }

    @Override
    public void onHide() {
        super.onHide();
//        SeamlessPortals.LOGGER.info("Screen hidden");
        this.updatePortalGenerator();
    }

    @Override
    public void onRemove() {
        super.onRemove();
//        SeamlessPortals.LOGGER.info("Screen removed");
        this.updatePortalGenerator();
    }
}
