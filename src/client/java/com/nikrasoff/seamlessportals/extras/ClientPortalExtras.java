package com.nikrasoff.seamlessportals.extras;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.nikrasoff.seamlessportals.SeamlessPortalsConstants;
import com.nikrasoff.seamlessportals.extras.interfaces.IPortalablePlayerController;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.player.PlayerEntity;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.singletons.GameSingletons;

public class ClientPortalExtras {
    // Random useful stuff
    public static final float SLICING_RENDER_OFFSET = 0.005f;

    private static final Vector3 tempVector = new Vector3();

    public static boolean shouldInvertNormal(Portal portal, Vector3 entityPos, boolean isDuplicate){
        return isDuplicate ? portal.getPortalSide(tempVector.set(entityPos).add(SeamlessPortalsConstants.portalCheckEpsilon)) == 1 : portal.getPortalSide(tempVector.set(entityPos).add(SeamlessPortalsConstants.portalCheckEpsilon)) == -1;
    }

    public static Vector3 getOriginPosForSlicing(Portal portal, Camera camera, Vector3 entityPos, boolean isDuplicate){
        Vector3 res = isDuplicate ? portal.linkedPortal.position.cpy() : portal.position.cpy();
        Vector3 normal = isDuplicate ? portal.linkedPortal.viewDirection.cpy() : portal.viewDirection.cpy();
        boolean invertNormal = shouldInvertNormal(portal, entityPos, isDuplicate);
        if (invertNormal){
            normal.scl(-1);
        }
        tempVector.set(res).sub(camera.position).nor();
        if (tempVector.dot(normal) > 0){
            res.add(normal.scl(SLICING_RENDER_OFFSET));
        }
        else{
            res.add(normal.scl(-SLICING_RENDER_OFFSET));
        }
        return res;
    }

    public static boolean isEntityLocalPlayer(Entity entity){
        if (entity instanceof PlayerEntity pe){
            return pe.getPlayer() == GameSingletons.client().getLocalPlayer();
        }
        return false;
    }

    public static boolean isPlayerCameraTeleported(){
        return ((IPortalablePlayerController) InGame.playerController).cosmicReach_Seamless_Portals$hasCameraBeenTeleported();
    }

    public static boolean isEntityJustTeleportedPlayer(Entity entity){
        return isEntityLocalPlayer(entity) && isPlayerCameraTeleported();
    }
}
