package com.nikrasoff.seamlessportals.items.containers;

import com.github.puzzle.game.items.data.DataTagManifest;
import com.github.puzzle.game.util.DataTagUtil;
import com.nikrasoff.seamlessportals.SeamlessPortalsItems;
import com.nikrasoff.seamlessportals.blockentities.BlockEntityPortalGenerator;
import finalforeach.cosmicreach.items.ItemSlot;
import finalforeach.cosmicreach.items.containers.SlotContainer;
import finalforeach.cosmicreach.savelib.crbin.CRBinDeserializer;
import finalforeach.cosmicreach.savelib.crbin.CRBinSerializer;

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
        checkItem();
    }

    public void checkItem(){
        if (!isItemValid() && blockEntityPortalGenerator.isPortalActive()){
            blockEntityPortalGenerator.closePortal();
            blockEntityPortalGenerator.justUpdated = true;
        }
    }

    public boolean isItemValid(){
        ItemSlot input = this.getInputSlot();
        return !(input.isEmpty() || input.itemStack.getItem() != SeamlessPortalsItems.CALIBRATED_OMNIUM_CRYSTAL);
    }

    public int getFrequency(){
        if (!this.isItemValid()) return -1;
        DataTagManifest data = DataTagUtil.getManifestFromStack(this.getInputSlot().itemStack);
        if (!data.hasTag("frequency")) return -1;
        return (int) data.getTag("frequency").getValue();
    }

    public ItemSlot getInputSlot(){
        return this.getSlot(0);
    }
}
