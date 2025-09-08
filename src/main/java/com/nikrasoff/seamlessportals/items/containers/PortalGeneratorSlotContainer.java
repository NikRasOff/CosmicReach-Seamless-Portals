package com.nikrasoff.seamlessportals.items.containers;

import com.badlogic.gdx.math.Vector3;
import com.nikrasoff.seamlessportals.SeamlessPortalsItems;
import com.nikrasoff.seamlessportals.blockentities.BlockEntityPortalGenerator;
import finalforeach.cosmicreach.items.ItemSlot;
import finalforeach.cosmicreach.items.containers.SlotContainer;
import finalforeach.cosmicreach.savelib.crbin.CRBinDeserializer;
import finalforeach.cosmicreach.savelib.crbin.CRBinSerializer;
import finalforeach.cosmicreach.world.Zone;
import io.github.puzzle.cosmic.api.util.DataPointUtil;
import io.github.puzzle.cosmic.impl.data.point.DataPointManifest;

public class PortalGeneratorSlotContainer extends SlotContainer {

    private BlockEntityPortalGenerator blockEntityPortalGenerator;

    private PortalGeneratorSlotContainer(){
    }

    public PortalGeneratorSlotContainer(BlockEntityPortalGenerator portalGenerator){
        super(1);
        this.blockEntityPortalGenerator = portalGenerator;
    }

    public void setPortalGenerator(BlockEntityPortalGenerator generator){
        this.blockEntityPortalGenerator = generator;
    }

    @Override
    public void write(CRBinSerializer serial) {
        super.write(serial);
    }

    public void read(CRBinDeserializer deserial) {
        super.read(deserial);
    }

    @Override
    public void onItemSlotUpdate(ItemSlot itemSlot) {
        super.onItemSlotUpdate(itemSlot);
    }

    public boolean isItemValid(){
        ItemSlot input = this.getInputSlot();
        return !(input.isEmpty() || input.getItemStack().getItem() != SeamlessPortalsItems.CALIBRATED_OMNIUM_CRYSTAL);
    }

    public int getFrequency(){
        if (!this.isItemValid()) return -1;
        DataPointManifest data = (DataPointManifest) DataPointUtil.getManifestFromStack(this.getInputSlot().getItemStack());
        if (!data.has("frequency")) return -1;
        return (int) data.get("frequency").getValue();
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
