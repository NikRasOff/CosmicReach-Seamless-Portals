package com.nikrasoff.seamlessportals.items.containers;

import com.nikrasoff.seamlessportals.SeamlessPortalsConstants;
import com.nikrasoff.seamlessportals.SeamlessPortalsItems;
import com.nikrasoff.seamlessportals.blockentities.BlockEntityOmniumCalibrator;
import finalforeach.cosmicreach.io.CRBinDeserializer;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.items.ItemSlot;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.items.containers.SlotContainer;

import static com.nikrasoff.seamlessportals.SeamlessPortalsConstants.CALIBRATED_OMNIUM_ID;

public class OmniumCalibratorSlotContainer extends SlotContainer {

    private BlockEntityOmniumCalibrator blockEntityOmniumCalibrator;

    private OmniumCalibratorSlotContainer(){
    }

    public OmniumCalibratorSlotContainer(BlockEntityOmniumCalibrator omniumCalibrator){
        super(4);
        blockEntityOmniumCalibrator = omniumCalibrator;
        this.setup();
    }

    public void setOmniumCalibrator(BlockEntityOmniumCalibrator calibrator){
        this.blockEntityOmniumCalibrator = calibrator;
    }

    public void read(CRBinDeserializer deserial) {
        super.read(deserial);
        this.setup();
    }

    @Override
    public void onItemSlotUpdate(ItemSlot itemSlot) {
        super.onItemSlotUpdate(itemSlot);
        this.blockEntityOmniumCalibrator.setTicking(true);
    }

    public boolean isOutputEmpty(){
        return this.getOutputSlot1().isEmpty() && this.getOutputSlot2().isEmpty();
    }

    public boolean canProcessBegin(){
        return this.isOutputEmpty() && this.getProcessSlot().isEmpty();
    }

    public boolean isProcessGoing(){
        return !this.getProcessSlot().isEmpty();
    }

    public void onProcessComplete(){
        this.getProcessSlot().addAmount(-2);
        this.getOutputSlot1().addItemStack(SeamlessPortalsItems.CALIBRATED_OMNIUM_CRYSTAL, 1);
        this.getOutputSlot2().addItemStack(SeamlessPortalsItems.CALIBRATED_OMNIUM_CRYSTAL, 1);
    }

    public void checkProcess(){
        ItemSlot input = this.getInputSlot();
        if (input.isEmpty() || !this.canProcessBegin()) return;
        ItemStack inputStack = input.itemStack;
        if (inputStack.getItem() == SeamlessPortalsItems.OMNIUM_CRYSTAL){
            if (inputStack.amount >= 2){
                ItemSlot processSlot = this.getProcessSlot();
                processSlot.setItemStack(inputStack.copy());
                processSlot.itemStack.amount = 2;
                processSlot.onItemSlotUpdate();
                input.addAmount(-2);
                input.onItemSlotUpdate();
            }
        }
    }

    protected void setup() {
        this.getProcessSlot().setOutputOnly(true);
        this.getOutputSlot1().setOutputOnly(true);
        this.getOutputSlot2().setOutputOnly(true);
    }

    public ItemSlot getInputSlot(){
        return this.getSlot(0);
    }
    public ItemSlot getProcessSlot(){
        return this.getSlot(1);
    }
    public ItemSlot getOutputSlot1(){
        return this.getSlot(2);
    }
    public ItemSlot getOutputSlot2(){
        return this.getSlot(3);
    }
}
