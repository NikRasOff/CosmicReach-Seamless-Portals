package com.nikrasoff.seamlessportals.blockentities;

import com.nikrasoff.seamlessportals.items.containers.OmniumCalibratorSlotContainer;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.blockentities.BlockEntity;
import finalforeach.cosmicreach.blockentities.BlockEntityCreator;
import finalforeach.cosmicreach.blockentities.IBlockEntityWithContainer;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.io.CRBinDeserializer;
import finalforeach.cosmicreach.io.CRBinSerializer;
import finalforeach.cosmicreach.items.ItemSlot;
import finalforeach.cosmicreach.world.BlockSetter;
import finalforeach.cosmicreach.world.Zone;

import java.util.HashMap;
import java.util.function.Predicate;

public class BlockEntityOmniumCalibrator extends BlockEntity implements IBlockEntityWithContainer {
    private final static String BLOCK_ENTITY_ID = "seamlessportals:omnium_calibrator";
    private final static int MAX_PROGRESS_TICKS = 64;
    public OmniumCalibratorSlotContainer slotContainer;
    int progressTicks = 0;

    public BlockEntityOmniumCalibrator(Zone zone, int globalX, int globalY, int globalZ){
        super(zone, globalX, globalY, globalZ);
        this.slotContainer = new OmniumCalibratorSlotContainer(this);
    }

    public BlockEntityOmniumCalibrator(BlockPosition blockPosition){
        this(blockPosition.getZone(), blockPosition.getGlobalX(), blockPosition.getGlobalY(), blockPosition.getGlobalZ());
    }

    public void onRemove() {
        super.onRemove();
        this.slotContainer.dropAllItems(this.zone, (float)this.getGlobalX() + 0.5F, (float)this.getGlobalY() + 0.5F, (float)this.getGlobalZ() + 0.5F);
    }

    public void onInteract(Player player, Zone zone) {
        super.onInteract(player, zone);
        GameSingletons.openBlockEntityScreen(player, zone, this);
    }

    public void read(CRBinDeserializer deserial) {
        super.read(deserial);
        this.slotContainer = deserial.readObj("slotContainer", OmniumCalibratorSlotContainer.class);
        this.slotContainer.setOmniumCalibrator(this);
        this.progressTicks = deserial.readInt("progressTicks", 0);
    }

    public void write(CRBinSerializer serial) {
        super.write(serial);
        serial.writeObj("slotContainer", this.slotContainer);
        serial.writeInt("progressTicks", this.progressTicks);
    }

    public static void registerBlockEntityCreator() {
        BlockEntityCreator.registerBlockEntityCreator(BLOCK_ENTITY_ID, (blockState, zone, x, y, z) -> new BlockEntityOmniumCalibrator(zone, x, y, z));
    }

    public void updateBlockState(boolean isWorking){
        BlockState currentBlockState = this.getBlockState();
        String facing = currentBlockState.getParam("facing");
        HashMap<String, String> newParams = new HashMap<>();
        newParams.put("active", (isWorking ? "true" : "false"));
        newParams.put("facing", facing);
        BlockState newBlockState = currentBlockState.getVariantWithParams(newParams);
        if (newBlockState != currentBlockState){
            BlockSetter.get().replaceBlock(this.zone, newBlockState, getGlobalX(), getGlobalY(), getGlobalZ());
        }
    }

    public void onTick() {
        super.onTick();
        boolean working = this.slotContainer.isProcessGoing();
        if (!working) {
            this.setTicking(false);
            this.updateBlockState(false);

            this.progressTicks = 0;
            this.slotContainer.checkProcess();
        } else {
            ++this.progressTicks;
            if (this.progressTicks == 1) {
                this.updateBlockState(true);
            }

            if (this.progressTicks >= 64) {
                this.slotContainer.onProcessComplete();
                this.progressTicks = 0;
            }
        }
    }

    @Override
    public String getBlockEntityId() {
        return BLOCK_ENTITY_ID;
    }

    @Override
    public ItemSlot getFirstMatchingItemSlot(Predicate<ItemSlot> slotPredicate) {
        return this.slotContainer.getFirstMatchingItemSlot(slotPredicate);
    }

    public float getProgressRatio(){
        return (float) this.progressTicks / MAX_PROGRESS_TICKS;
    }
}
