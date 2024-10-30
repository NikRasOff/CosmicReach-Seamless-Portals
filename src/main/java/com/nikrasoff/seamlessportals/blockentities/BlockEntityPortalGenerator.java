package com.nikrasoff.seamlessportals.blockentities;

import com.badlogic.gdx.math.Vector2;
import com.nikrasoff.seamlessportals.items.containers.PortalGeneratorSlotContainer;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.blockentities.BlockEntity;
import finalforeach.cosmicreach.blockentities.BlockEntityCreator;
import finalforeach.cosmicreach.blockentities.IBlockEntityWithContainer;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.io.CRBinDeserializer;
import finalforeach.cosmicreach.io.CRBinSerializer;
import finalforeach.cosmicreach.items.ItemSlot;
import finalforeach.cosmicreach.world.Zone;

import java.util.function.Predicate;

public class BlockEntityPortalGenerator extends BlockEntity implements IBlockEntityWithContainer {
    private final static String BLOCK_ENTITY_ID = "seamlessportals:portal_generator";
    public PortalGeneratorSlotContainer slotContainer;
    public Vector2 portalSize = new Vector2(3, 3);
    public Vector2 entrancePortalOffset = new Vector2();

    public BlockEntityPortalGenerator(Zone zone, int globalX, int globalY, int globalZ){
        super(zone, globalX, globalY, globalZ);
        this.slotContainer = new PortalGeneratorSlotContainer(this);
    }

    public BlockEntityPortalGenerator(BlockPosition blockPosition){
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
        this.slotContainer = deserial.readObj("slotContainer", PortalGeneratorSlotContainer.class);
        this.slotContainer.setPortalGenerator(this);
    }

    public void write(CRBinSerializer serial) {
        super.write(serial);
        serial.writeObj("slotContainer", this.slotContainer);
    }

    public static void registerBlockEntityCreator() {
        BlockEntityCreator.registerBlockEntityCreator(BLOCK_ENTITY_ID, (blockState, zone, x, y, z) -> new BlockEntityPortalGenerator(zone, x, y, z));
    }

    @Override
    public String getBlockEntityId() {
        return BLOCK_ENTITY_ID;
    }

    @Override
    public ItemSlot getFirstMatchingItemSlot(Predicate<ItemSlot> slotPredicate) {
        return this.slotContainer.getFirstMatchingItemSlot(slotPredicate);
    }
}
