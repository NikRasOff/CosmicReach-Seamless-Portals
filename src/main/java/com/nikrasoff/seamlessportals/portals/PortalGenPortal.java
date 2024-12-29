package com.nikrasoff.seamlessportals.portals;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.blockentities.BlockEntityPortalGenerator;
import com.nikrasoff.seamlessportals.blockentities.BlockEntitySpacialAnchor;
import com.nikrasoff.seamlessportals.extras.PortalSpawnBlockInfo;
import com.nikrasoff.seamlessportals.extras.interfaces.IPortalableEntity;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.blockentities.BlockEntity;
import finalforeach.cosmicreach.savelib.crbin.CRBSerialized;
import finalforeach.cosmicreach.savelib.crbin.CRBinDeserializer;
import finalforeach.cosmicreach.savelib.crbin.CRBinSerializer;
import finalforeach.cosmicreach.world.Zone;

public class PortalGenPortal extends Portal {
    private PortalSpawnBlockInfo sourceBlock = new PortalSpawnBlockInfo();
    @CRBSerialized
    private boolean isSecond = false;

    public static PortalGenPortal readPortal(CRBinDeserializer deserializer){
        PortalGenPortal portal = new PortalGenPortal();
        if (deserializer != null) {
            portal.read(deserializer);
            portal.sourceBlock.position.set(deserializer.readInt("sourceBlockX", 0), deserializer.readInt("sourceBlockY", 0), deserializer.readInt("sourceBlockZ", 0));
            portal.sourceBlock.zoneId = deserializer.readString("sourceBlockZone");
            portal.sourceBlock.blockState = deserializer.readString("sourceBlockParams");
            String zoneId = deserializer.readString("zoneId");
            if (zoneId == null){
                zoneId = GameSingletons.world.defaultZoneId;
            }
            SeamlessPortals.portalManager.addPortal(portal);
            Portal lPortal;
            if (GameSingletons.isHost){
                lPortal = SeamlessPortals.portalManager.getPortalWithGen(portal.linkedPortalID, portal.linkedPortalChunkCoords, zoneId);
            }
            else {
                lPortal = SeamlessPortals.portalManager.getPortal(portal.linkedPortalID);
            }
            if (lPortal != null){
                portal.linkPortal(lPortal);
                lPortal.linkPortal(portal);
            }
            portal.calculateLocalBB();
            portal.calculateMeshBB();
        }
        return portal;
    }

    public PortalGenPortal(){
        super("seamlessportals:entity_portal_gen_portal");
        this.canDespawn = false;
        this.hasGravity = false;
        this.noClip = true;
        IPortalableEntity.setIgnorePortals((IPortalableEntity) this, true);
        if (GameSingletons.isClient){
            this.modelInstance = SeamlessPortals.clientConstants.getNewPortalModelInstance();
        }
    }

    private PortalGenPortal(Vector2 size, String viewDir, Vector3 portalPos){
        super(size, viewDir, portalPos);
        this.entityTypeId = "seamlessportals:entity_portal_gen_portal";
    }

    public static PortalGenPortal fromBlockInfo(PortalSpawnBlockInfo info, BlockEntityPortalGenerator gen, boolean isSecondP){
        String[] strId = info.blockState.split(",");
        String dirString = "";
        for (String id : strId){
            String[] i = id.split("=");
            if (i.length > 1){
                if (i[0].equals("facing")){
                    dirString = i[1];
                }
            }
        }
        PortalGenPortal newPortal = new PortalGenPortal(gen.portalSize, dirString, info.position.toVector3().add(new Vector3(0.5f, 0.5f, 0.5f)));
        newPortal.sourceBlock = info;

        Vector2 offset = isSecondP ? gen.exitPortalOffset : gen.entrancePortalOffset;
        Vector3 nudgeCoords1 = new Vector3(offset.x, offset.y, 0).mul(newPortal.getRotationMatrix());
        newPortal.position.add(nudgeCoords1);
        if (!newPortal.figureOutPlacement(gen.zone, newPortal.portalSize.x / 2 - offset.x, newPortal.portalSize.x / 2 + offset.x, newPortal.portalSize.y / 2 - offset.y, newPortal.portalSize.y / 2 + offset.y)){
            SeamlessPortals.portalManager.removePortal(newPortal);
            return null;
        }

        if (isSecondP){
            newPortal.isSecond = true;
        }
        else {
            newPortal.viewDirection.scl(-1);
        }

        return newPortal;
    }

    @Override
    public void write(CRBinSerializer serial) {
        super.write(serial);
        serial.writeInt("sourceBlockX", this.sourceBlock.position.x);
        serial.writeInt("sourceBlockY", this.sourceBlock.position.y);
        serial.writeInt("sourceBlockZ", this.sourceBlock.position.z);
        serial.writeString("sourceBlockZone", this.sourceBlock.zoneId);
        serial.writeString("sourceBlockParams", this.sourceBlock.blockState);
    }

    @Override
    public void update(Zone zone, double deltaTime) {
        super.update(zone, deltaTime);

//        BlockState bs = zone.getBlockState(this.sourceBlock.position.toVector3());
//        if (!bs.getStateParamsStr().equals(this.sourceBlock.blockState)) {
//            this.destroySelfAndLinkedPortal();
//            return;
//        }

        BlockEntity be = zone.getBlockEntity(this.sourceBlock.position.x, this.sourceBlock.position.y, this.sourceBlock.position.z);
        if (be == null){
            this.destroySelfAndLinkedPortal();
            return;
        }
        if (this.isSecond){
            if (be instanceof BlockEntitySpacialAnchor spacialAnchor){
                if (!spacialAnchor.slotContainer.isPrimed()){
                    this.destroySelfAndLinkedPortal();
                    return;
                }
            }
            else {
                this.destroySelfAndLinkedPortal();
                return;
            }
        }
        else {
            if (be instanceof BlockEntityPortalGenerator portalGenerator){
                if (!portalGenerator.slotContainer.isItemValid()){
                    this.destroySelfAndLinkedPortal();
                    return;
                }
            }
            else {
                this.destroySelfAndLinkedPortal();
                return;
            }
        }

        if (this.isPortalInAWall(this.zone)){
            this.destroySelfAndLinkedPortal();
        }
    }

    @Override
    public void startDestruction() {
        super.startDestruction();
        if (!this.isSecond){
            BlockEntity be = this.zone.getBlockEntity(this.sourceBlock.position.x, this.sourceBlock.position.y, this.sourceBlock.position.z);
            if (be instanceof BlockEntityPortalGenerator portalGenerator){
                if (portalGenerator.isPortalActive()){
                    portalGenerator.portalId.set(-1, -1, -1);
                    portalGenerator.justUpdated = true;
                    portalGenerator.updateBlockState(false);
                }
            }
        }
    }

    public boolean figureOutPlacement(Zone z, float maxBumpPosX, float maxBumpNegX, float maxBumpPosY, float maxBumpNegY){
        // Tries to place the portal so that it doesn't intersect any walls
        // Shifts it around in its plane to do so
        // returns true is successful, otherwise false
        if (!isPortalInAWall(z)) return true;
        Vector3 originalPos = this.position.cpy();

        if (tryBumping(new Vector3(1, 0, 0), maxBumpPosX, z)) return true;
        this.position.set(originalPos);
        if (tryBumping(new Vector3(-1, 0, 0), maxBumpNegX, z)) return true;
        this.position.set(originalPos);
        if (tryBumping(new Vector3(0, 1, 0), maxBumpPosY, z)) return true;
        this.position.set(originalPos);
        if (tryBumping(new Vector3(0, -1, 0), maxBumpNegY, z)) return true;
        this.position.set(originalPos);

        return false;
    }

    private boolean tryBumping(Vector3 dir, float maxAmount, Zone z){
        // Tries placing the portal along a direction
        float bumpAmount = 0.01f;
        float bumpCount = 0;
        Matrix4 portalMatrix = this.getRotationMatrix();
        Vector3 bumpDir = new Vector3();
        bumpDir.set(dir).scl(bumpAmount).mul(portalMatrix);
        while (bumpCount < maxAmount){
            bumpCount += bumpAmount;
            this.position.add(bumpDir);
            if (!isPortalInAWall(z)) {
                return true;
            }
        }
        return false;
    }
}
