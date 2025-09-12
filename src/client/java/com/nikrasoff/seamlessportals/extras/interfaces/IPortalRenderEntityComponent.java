package com.nikrasoff.seamlessportals.extras.interfaces;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.entities.Entity;

public interface IPortalRenderEntityComponent {
    void renderNoAnim(Entity entity, Camera worldCamera, Vector3 tmpRenderPos, Matrix4 tmpModelMatrix, boolean shouldRender);
    void renderSliced(Entity entity, Camera worldCamera, Portal portal, Vector3 tmpRenderPos, Matrix4 tmpModelMatrix, boolean shouldRender);
    void renderDuplicate(Entity entity, Camera worldCamera, Portal portal, Vector3 tmpRenderPos, Matrix4 tmpModelMatrix, boolean shouldRender);
}
