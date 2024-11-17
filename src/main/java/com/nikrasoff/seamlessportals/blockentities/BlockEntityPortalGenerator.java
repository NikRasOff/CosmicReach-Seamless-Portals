package com.nikrasoff.seamlessportals.blockentities;

import com.badlogic.gdx.math.Vector2;
import com.nikrasoff.seamlessportals.items.containers.PortalGeneratorSlotContainer;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.blockentities.BlockEntity;
import finalforeach.cosmicreach.blockentities.BlockEntityCreator;
import finalforeach.cosmicreach.blockentities.IBlockEntityWithContainer;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.items.ItemSlot;
import finalforeach.cosmicreach.savelib.crbin.CRBinDeserializer;
import finalforeach.cosmicreach.savelib.crbin.CRBinSerializer;
import finalforeach.cosmicreach.world.Zone;

import java.util.function.Predicate;

public class BlockEntityPortalGenerator extends BlockEntity implements IBlockEntityWithContainer {
    private final static String BLOCK_ENTITY_ID = "seamlessportals:portal_generator";
    public PortalGeneratorSlotContainer slotContainer;
    public Vector2 portalSize = new Vector2(3, 3);
    public Vector2 entrancePortalOffset = new Vector2();
    public Vector2 exitPortalOffset = new Vector2();

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
        this.portalSize.x = deserial.readFloat("portalSizeX", 3);
        this.portalSize.y = deserial.readFloat("portalSizeY", 3);
        this.entrancePortalOffset.x = deserial.readFloat("portal1OffsetX", 0);
        this.entrancePortalOffset.y = deserial.readFloat("portal1OffsetY", 0);
        this.exitPortalOffset.x = deserial.readFloat("portal2OffsetX", 0);
        this.exitPortalOffset.y = deserial.readFloat("portal2OffsetY", 0);
    }

    public void write(CRBinSerializer serial) {
        super.write(serial);
        serial.writeObj("slotContainer", this.slotContainer);
        serial.writeFloat("portalSizeX", this.portalSize.x);
        serial.writeFloat("portalSizeY", this.portalSize.y);
        serial.writeFloat("portal1OffsetX", this.entrancePortalOffset.x);
        serial.writeFloat("portal1OffsetY", this.entrancePortalOffset.y);
        serial.writeFloat("portal2OffsetX", this.exitPortalOffset.x);
        serial.writeFloat("portal2OffsetY", this.exitPortalOffset.y);
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
