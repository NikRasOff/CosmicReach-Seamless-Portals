package com.nikrasoff.seamlessportals.portals;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;
import com.badlogic.gdx.utils.Array;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import finalforeach.cosmicreach.entities.Player;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.io.ChunkLoader;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.WorldLoader;
import finalforeach.cosmicreach.world.Zone;
import finalforeach.cosmicreach.worldgen.ChunkColumn;

public class PortalManager {
    public String prevPortalGenZone;
    public Vector3 prevPortalGenPos;
    public Array<Portal> createdPortals = new Array<>(Portal.class);

    public boolean shouldUpdatePortalArray = false;

    public static boolean debugReady = false;
    private static ShapeRenderer shapeRenderer;

    public PortalManager(){}
    
    public BlockPosition getPrevGenBlockPos(){
        if (this.prevPortalGenPos == null) return null;
        Zone cur_zone = InGame.world.getZone(this.prevPortalGenZone);
        Chunk c = cur_zone.getChunkAtBlock((int) this.prevPortalGenPos.x, (int) this.prevPortalGenPos.y, (int) this.prevPortalGenPos.z);
        if (c == null){
            // Doesn't work in unloaded chunks, sadly.
            return null;
        }
        return new BlockPosition(c, (int) (this.prevPortalGenPos.x - c.blockX), (int) (this.prevPortalGenPos.y - c.blockY), (int) (this.prevPortalGenPos.z - c.blockZ));
    }

    public void createPortalPair(BlockPosition portalPos1, BlockPosition portalPos2, Zone zone1, Zone zone2){
        Portal portal1 = Portal.fromBlockPos(new Vector2(3, 3), portalPos1, zone1);
        Portal portal2 = Portal.fromBlockPos(new Vector2(3, 3), portalPos2, zone2);
        portal1.linkPortal(portal2);
        portal2.linkPortal(portal1);
        this.createdPortals.add(portal1);
        this.createdPortals.add(portal2);
    }

    public void linkPortalsInArray(){
        for (int i = 0; i < this.createdPortals.size; i += 2){
            this.createdPortals.get(i).linkPortal(this.createdPortals.get(i + 1));
            this.createdPortals.get(i + 1).linkPortal(this.createdPortals.get(i));
        }
    }

    public void updatePortalArray(){
        this.shouldUpdatePortalArray = false;
        Array<Portal> newPortalArray = new Array<>(Portal.class);
        for (Portal portal : this.createdPortals){
            if (!portal.isPortalDestroyed){
                newPortalArray.add(portal);
            }
        }
        this.createdPortals = newPortalArray;
    }

    private void initialiseDebug(){
        shapeRenderer = new ShapeRenderer();
        debugReady = true;
    }

    private void disableDebug(){
        if (shapeRenderer != null){
            shapeRenderer.dispose();
            shapeRenderer = null;
        }
        debugReady = false;
    }

    public void renderPortals(Camera playerCamera){
        if (SeamlessPortals.debugMode){
            if (!debugReady) initialiseDebug();
        }
        else if (debugReady){
            disableDebug();
        }
        if (this.shouldUpdatePortalArray) this.updatePortalArray();
        Player player = InGame.getLocalPlayer();
        if (debugReady){
            shapeRenderer.setColor(1, 0, 0, 1);
            shapeRenderer.setProjectionMatrix(playerCamera.combined);
        }
        for (Portal portal : this.createdPortals){
            portal.updateAnimations(Gdx.graphics.getDeltaTime());
            OrientedBoundingBox portalBB = portal.getMeshBoundingBox();
            OrientedBoundingBox portalBigBB = portal.getGlobalBoundingBox();
            if (debugReady){
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                shapeRenderer.setTransformMatrix(portalBigBB.transform);
                shapeRenderer.box(portalBigBB.getBounds().min.x, portalBigBB.getBounds().min.y, portalBigBB.getBounds().min.z, portalBigBB.getBounds().getWidth(), portalBigBB.getBounds().getHeight(), -portalBigBB.getBounds().getDepth());
                shapeRenderer.end();
            }
            if (!portal.isPortalMeshGenerated){
                portal.updatePortalMeshScale((PerspectiveCamera) playerCamera);
            }
            if (!portal.zoneID.equals(player.zoneId) || portal.isPortalDestroyed || portal.position.dst(playerCamera.position) > 50){
                continue;
            }
            if (!playerCamera.frustum.boundsInFrustum(portalBB) && !portal.isInterpProtectionActive){
                continue;
            }
            portal.render(playerCamera);
        }
    }
}
