package com.nikrasoff.seamlessportals.items.containers;

import com.github.puzzle.game.items.data.DataTagManifest;
import com.github.puzzle.game.util.DataTagUtil;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.SeamlessPortalsItems;
import com.nikrasoff.seamlessportals.blockentities.BlockEntitySpacialAnchor;
import com.nikrasoff.seamlessportals.extras.IntVector3;
import com.nikrasoff.seamlessportals.extras.PortalSpawnBlockInfo;
import finalforeach.cosmicreach.items.ItemSlot;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.items.containers.SlotContainer;
import finalforeach.cosmicreach.savelib.crbin.CRBinDeserializer;
import finalforeach.cosmicreach.savelib.crbin.CRBinSerializer;

public class SpacialAnchorSlotContainer extends SlotContainer {

    private BlockEntitySpacialAnchor blockEntitySpacialAnchor;
    private boolean primed = false;
    private int frequency = -1;

    private SpacialAnchorSlotContainer(){
    }

    public SpacialAnchorSlotContainer(BlockEntitySpacialAnchor spacialAnchor){
        super(1);
        this.blockEntitySpacialAnchor = spacialAnchor;
    }

    public void setSpacialAnchor(BlockEntitySpacialAnchor anchor){
        this.blockEntitySpacialAnchor = anchor;
    }

    @Override
    public void write(CRBinSerializer serial) {
        super.write(serial);
        serial.writeBoolean("primed", this.primed);
        serial.writeInt("frequency", this.frequency);
    }

    public void read(CRBinDeserializer deserial) {
        super.read(deserial);
        this.primed = deserial.readBoolean("primed", false);
        this.frequency = deserial.readInt("frequency", -1);
    }

    public void registerSpacialAnchor(ItemStack crystal){
        DataTagManifest data = DataTagUtil.getManifestFromStack(crystal);
        if (!data.hasTag("frequency")){
            if (this.primed) deregisterSpacialAnchor();
            this.primed = false;
            return;
        }
        int frequency = (int) data.getTag("frequency").getValue();
        if (frequency != this.frequency) {
            deregisterSpacialAnchor();
            SeamlessPortals.portalManager.registerSpacialAnchor(frequency, new PortalSpawnBlockInfo(this.blockEntitySpacialAnchor.zone.zoneId, new IntVector3(this.blockEntitySpacialAnchor.getGlobalX(), this.blockEntitySpacialAnchor.getGlobalY(), this.blockEntitySpacialAnchor.getGlobalZ()), this.blockEntitySpacialAnchor.getBlockState().getStateParamsStr()));
            this.frequency = frequency;
            this.primed = true;
        }
    }

    public void deregisterSpacialAnchor(){
        this.primed = false;
        SeamlessPortals.portalManager.deregisterSpacialAnchor(this.frequency, new IntVector3(this.blockEntitySpacialAnchor.getGlobalX(), this.blockEntitySpacialAnchor.getGlobalY(), this.blockEntitySpacialAnchor.getGlobalZ()));
    }

    @Override
    public void onItemSlotUpdate(ItemSlot itemSlot) {
        super.onItemSlotUpdate(itemSlot);
        this.checkInput();
    }

    public void checkInput(){
        ItemSlot input = this.getInputSlot();
        if (input.itemStack == null || input.itemStack.getItem() == null || input.itemStack.getItem() != SeamlessPortalsItems.CALIBRATED_OMNIUM_CRYSTAL) {
            this.blockEntitySpacialAnchor.destroyPortals();
            deregisterSpacialAnchor();
        } else if (!primed) {
            registerSpacialAnchor(input.itemStack);
        }
    }

    public boolean isPrimed(){
        return this.primed;
    }

    public ItemSlot getInputSlot(){
        return this.getSlot(0);
    }
}
