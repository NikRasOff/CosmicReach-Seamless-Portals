package com.nikrasoff.seamlessportals.items.containers;

import com.badlogic.gdx.math.Vector3;
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
import finalforeach.cosmicreach.world.Zone;
import io.github.puzzle.cosmic.api.util.DataPointUtil;
import io.github.puzzle.cosmic.impl.data.point.DataPointManifest;

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
        DataPointManifest data = (DataPointManifest) DataPointUtil.getManifestFromStack(crystal);
        if (!data.has("frequency")){
            if (this.primed) deregisterSpacialAnchor();
            this.primed = false;
            return;
        }
        int frequency = (int) data.get("frequency").getValue();
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
        this.frequency = -1;
    }

    @Override
    public void onItemSlotUpdate(ItemSlot itemSlot) {
        super.onItemSlotUpdate(itemSlot);
        this.checkInput();
    }

    public void checkInput(){
        ItemSlot input = this.getInputSlot();
        if (input.getItemStack() == null || input.getItemStack().getItem() == null || input.getItemStack().getItem() != SeamlessPortalsItems.CALIBRATED_OMNIUM_CRYSTAL) {
            deregisterSpacialAnchor();
        } else if (!primed) {
            registerSpacialAnchor(input.getItemStack());
        }
    }

    public boolean isPrimed(){
        return this.primed;
    }

    public ItemSlot getInputSlot(){
        return this.getSlot(0);
    }

    @Override
    public void dropAllItems(Zone zone, Vector3 position) {
        super.dropAllItems(zone, position);
    }

    public void clear() {
        super.clear();
    }
}
