package com.nikrasoff.seamlessportals.extras;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;
import com.nikrasoff.seamlessportals.extras.interfaces.IPortalableEntity;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.entities.Entity;

public class PortalEntityTools {
    public static void setTmpNextPosition(Entity entity, Vector3 pos){
        ((IPortalableEntity) entity).cosmicReach_Seamless_Portals$setTmpNextPosition(pos);
    }
    public static Vector3 getTmpNextPosition(Entity entity){
        return ((IPortalableEntity) entity).cosmicReach_Seamless_Portals$getTmpNextPosition();
    }
    public static void setJustTeleported(Entity entity, boolean value){
        ((IPortalableEntity) entity).cosmicReach_Seamless_Portals$setJustTeleported(value);
    }
    public static void setTeleportingPortal(Entity entity, Portal portal){
        ((IPortalableEntity) entity).cosmicReach_Seamless_Portals$setTeleportingPortal(portal);
    }
    public static Matrix4 getTmpTransformMatrix(Entity entity){
        return ((IPortalableEntity) entity).cosmicReach_Seamless_Portals$getTmpTransformMatrix();
    }
    public static OrientedBoundingBox getPortaledBoundingBox(Entity entity){
        return ((IPortalableEntity) entity).cosmicReach_Seamless_Portals$getPortaledBoundingBox();
    }
}
