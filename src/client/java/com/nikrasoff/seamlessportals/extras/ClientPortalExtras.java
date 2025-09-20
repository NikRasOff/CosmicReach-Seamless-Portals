package com.nikrasoff.seamlessportals.extras;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.nikrasoff.seamlessportals.extras.interfaces.IPortalablePlayerController;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.player.PlayerEntity;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.singletons.GameSingletons;

public class ClientPortalExtras {
    // Random useful stuff
    public static final float SLICING_RENDER_OFFSET = 0.005f;

    public static Vector3 getOriginPosForSlicing(Portal portal, Camera camera, Vector3 entityPos, boolean isDuplicate){
        Vector3 res = isDuplicate ? portal.linkedPortal.position.cpy() : portal.position.cpy();
        Vector3 normal = isDuplicate ? portal.linkedPortal.viewDirection.cpy() : portal.viewDirection.cpy();
        int invertNormal = isDuplicate ? Math.max(portal.getPortalSide(entityPos), 0) : Math.max(-portal.getPortalSide(entityPos), 0);
        if (invertNormal == 1){
            normal.scl(-1);
        }
        Vector3 dirFromCamera = res.cpy().sub(camera.position).nor();
        if (dirFromCamera.dot(normal) > 0){
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
//        return false;
    }
}
