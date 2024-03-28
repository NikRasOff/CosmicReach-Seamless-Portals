package com.nikrasoff.seamlessportals;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.world.BlockPosition;
import finalforeach.cosmicreach.world.chunks.Chunk;

public class PortalManager {
    public Vector3 prevPortalGen;
    public Array<Portal> createdPortals = new Array<>(Portal.class);

    public PortalManager(){}
    
    public BlockPosition getPrevGenBlockPos(){
        if (this.prevPortalGen == null) return null;
        Chunk c = InGame.world.getChunkAtBlock((int) this.prevPortalGen.x, (int) this.prevPortalGen.y, (int) this.prevPortalGen.z);
        return new BlockPosition(c, (int) (this.prevPortalGen.x - c.blockX), (int) (this.prevPortalGen.y - c.blockY), (int) (this.prevPortalGen.z - c.blockZ));
    }

    public void createPortalPair(BlockPosition portalPos1, BlockPosition portalPos2){
        Portal portal1 = Portal.fromBlockPos(new Vector2(3, 3), portalPos1);
        Portal portal2 = Portal.fromBlockPos(new Vector2(3, 3), portalPos2);
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
        Array<Portal> newPortalArray = new Array<>(Portal.class);
        for (Portal portal : this.createdPortals){
            if (!portal.isPortalDestroyed){
                newPortalArray.add(portal);
            }
        }
        this.createdPortals = newPortalArray;
    }

    public void renderPortals(Camera playerCamera){
        for (Portal portal : this.createdPortals){
            BoundingBox portalBB = portal.getGlobalBoundingBox();
            if (playerCamera.frustum.boundsInFrustum(portalBB)){
                portal.render(playerCamera);
            }
        }
    }
}
