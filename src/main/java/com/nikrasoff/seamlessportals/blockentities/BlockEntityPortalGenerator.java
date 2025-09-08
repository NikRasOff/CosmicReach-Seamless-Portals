package com.nikrasoff.seamlessportals.blockentities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.extras.IntVector3;
import com.nikrasoff.seamlessportals.extras.PortalSpawnBlockInfo;
import com.nikrasoff.seamlessportals.items.containers.PortalGeneratorSlotContainer;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.singletons.GameSingletons;
import finalforeach.cosmicreach.blockentities.BlockEntity;
import finalforeach.cosmicreach.blockentities.BlockEntityCreator;
import finalforeach.cosmicreach.blockentities.IBlockEntityWithContainer;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.EntityUniqueId;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.items.ItemSlot;
import finalforeach.cosmicreach.items.SlotContainerWindows;
import finalforeach.cosmicreach.items.containers.SlotContainer;
import finalforeach.cosmicreach.savelib.crbin.CRBinDeserializer;
import finalforeach.cosmicreach.savelib.crbin.CRBinSerializer;
import finalforeach.cosmicreach.sounds.GameSound;
import finalforeach.cosmicreach.world.BlockSetter;
import finalforeach.cosmicreach.world.Zone;

import java.util.HashMap;
import java.util.function.Predicate;

public class BlockEntityPortalGenerator extends BlockEntity implements IBlockEntityWithContainer {
    private final static GameSound portalGenActivateSound = GameSound.of("seamlessportals:sounds/blocks/portal_generator_power_up.ogg");
    private final static GameSound portalGenDeactivateSound = GameSound.of("seamlessportals:sounds/blocks/portal_generator_power_down.ogg");
    private final static String BLOCK_ENTITY_ID = "seamlessportals:portal_generator";
    public PortalGeneratorSlotContainer slotContainer;
    public Vector2 portalSize = new Vector2(3, 3);
    public Vector2 entrancePortalOffset = new Vector2();
    public Vector2 exitPortalOffset = new Vector2();
    public EntityUniqueId portalId = new EntityUniqueId();
    public boolean justUpdated = false;
    private boolean isBeingDeleted = false;

    public BlockEntityPortalGenerator(Zone zone, int globalX, int globalY, int globalZ){
        super(zone, globalX, globalY, globalZ);
        this.slotContainer = new PortalGeneratorSlotContainer(this);
    }

    public BlockEntityPortalGenerator(BlockPosition blockPosition){
        this(blockPosition.getZone(), blockPosition.getGlobalX(), blockPosition.getGlobalY(), blockPosition.getGlobalZ());
    }

    public void onRemove() {
        super.onRemove();
        setTicking(false);
        this.isBeingDeleted = true;
        this.slotContainer.dropAllItems(this.zone, (float)this.getGlobalX() + 0.5F, (float)this.getGlobalY() + 0.5F, (float)this.getGlobalZ() + 0.5F);
    }

    @Override
    public void onCreate(BlockState blockState) {
        setTicking(true);
        super.onCreate(blockState);
    }

    public void onInteract(Player player, Zone zone) {
        super.onInteract(player, zone);
        GameSingletons.openBlockEntityScreen(SlotContainerWindows.add(this), player, zone, this);
    }

    public boolean isPortalActive(){
        return SeamlessPortals.portalManager.getPortal(this.portalId) != null;
    }

    public void updateBlockState(boolean isWorking){
        if (this.isBeingDeleted) return;
        if (isWorking) portalGenActivateSound.playGlobalSound3D(this.zone, new Vector3(this.getGlobalX(), this.getGlobalY(), this.getGlobalZ()));
        else portalGenDeactivateSound.playGlobalSound3D(this.zone, new Vector3(this.getGlobalX(), this.getGlobalY(), this.getGlobalZ()));
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

    public float getMaxOffsetX(){
        return Math.max(0, this.portalSize.x / 2 - 0.5f);
    }

    public float getMaxOffsetY(){
        return Math.max(0, this.portalSize.y / 2 - 0.5f);
    }

    public void openPortal(){
        if (GameSingletons.isHost){
            if (this.isPortalActive()) return;
            int frequency = this.slotContainer.getFrequency();
            if (frequency == -1) {
                this.justUpdated = true;
                return;
            }
            if (!SeamlessPortals.portalManager.spacialAnchors.containsKey(String.valueOf(frequency))){
                this.justUpdated = true;
                return;
            }
            PortalSpawnBlockInfo gen1 = new PortalSpawnBlockInfo(this.zone.zoneId, new IntVector3(this.getGlobalX(), this.getGlobalY(), this.getGlobalZ()), this.getBlockState().getStateParamsStr());
            PortalSpawnBlockInfo gen2 = SeamlessPortals.portalManager.spacialAnchors.get(String.valueOf(frequency)).random();
            boolean res = SeamlessPortals.portalManager.createPortalPairFromGenAndAnchor(gen1, gen2);
            if (!res){
                this.portalId.set(-1, -1, -1);
                this.justUpdated = true;
            }
            else {
                this.updateBlockState(true);
            }
        }
    }

    public void closePortal(){
        if (!this.isPortalActive()) return;
        this.updateBlockState(false);
        Portal p = SeamlessPortals.portalManager.getPortal(this.portalId);
        if (p == null) {
            this.portalId.set(-1, -1, -1);
            return;
        }
        p.startDestruction();
        p.linkedPortal.startDestruction();
        this.portalId.set(-1, -1, -1);
    }

    public void read(CRBinDeserializer deserial) {
        super.read(deserial);
        if (this.slotContainer != null) {
            this.slotContainer.read(deserial.readRawObj("slotContainer"));
        } else {
            this.slotContainer = deserial.readObj("slotContainer", PortalGeneratorSlotContainer.class);
            this.slotContainer.setPortalGenerator(this);
        }
        this.portalSize.x = deserial.readFloat("portalSizeX", 3);
        this.portalSize.y = deserial.readFloat("portalSizeY", 3);
        this.entrancePortalOffset.x = deserial.readFloat("portal1OffsetX", 0);
        this.entrancePortalOffset.y = deserial.readFloat("portal1OffsetY", 0);
        this.exitPortalOffset.x = deserial.readFloat("portal2OffsetX", 0);
        this.exitPortalOffset.y = deserial.readFloat("portal2OffsetY", 0);
        this.portalId.set(deserial.readLong("portalIdTime", -1), deserial.readInt("portalIdRand", -1), deserial.readInt("portalIdNum", -1));
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
        serial.writeLong("portalIdTime", this.portalId.getTime());
        serial.writeInt("portalIdRand", this.portalId.getRand());
        serial.writeInt("portalIdNum", this.portalId.getNumber());
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

    @Override
    public SlotContainer getSlotContainer() {
        return this.slotContainer;
    }
}
