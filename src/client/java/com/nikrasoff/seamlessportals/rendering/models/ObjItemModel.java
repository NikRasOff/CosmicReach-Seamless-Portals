package com.nikrasoff.seamlessportals.rendering.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.nikrasoff.seamlessportals.extras.interfaces.ISliceableItemModel;
import com.nikrasoff.seamlessportals.portals.Portal;
import com.nikrasoff.seamlessportals.rendering.SeamlessPortalsRenderUtil;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.rendering.items.ItemModel;
import finalforeach.cosmicreach.util.Identifier;

import java.lang.ref.WeakReference;

public class ObjItemModel extends ItemModel implements ISliceableItemModel {
    public static Camera itemCam;
    public Matrix4 heldModelMatrix = new Matrix4();
    public Matrix4 onGroundModelMatrix = new Matrix4();

    private static final Matrix4 tmpMat4 = new Matrix4();
    private static final PerspectiveCamera heldItemCamera;

    private final static Array<AnimationController> animArray = new Array<>();

    private final ModelInstance modelInstance;
    private final ModelInstance viewModelInstance;
    private final AnimationController animationController;
    private final AnimationController viewAnimController;

    public ObjItemModel(Identifier modelId){
        modelInstance = SeamlessPortalsRenderUtil.getModelInstance(modelId);
        viewModelInstance = SeamlessPortalsRenderUtil.getModelInstance(modelId);
        animationController = new AnimationController(modelInstance);
        viewAnimController = new AnimationController(viewModelInstance);
        viewAnimController.allowSameAnimation = true;
        animArray.add(viewAnimController);
        animArray.add(animationController);
    }

    public static void updateAnimations(){
        float delta = Gdx.graphics.getDeltaTime();
        for (AnimationController a : animArray){
            a.update(delta);
        }
    }

    public void setAnimation(String id, int loopCount){
        this.animationController.setAnimation(id, loopCount);
    }

    public void setViewAnimation(String id, int loopCount){
        this.viewAnimController.setAnimation(id, loopCount);
    }

    public void queueViewAnimation(String id, int loopCount){
        this.viewAnimController.queue(id, loopCount, 1, null, 0);
    }

    private static void setupItemCam() {
        itemCam = new OrthographicCamera(100.0F, 100.0F);
        itemCam.position.set(50.0F, 50.0F, 50.0F);
        itemCam.lookAt(0.5F, 0.5F, 0.5F);
        ((OrthographicCamera)itemCam).zoom = 0.017F;
        itemCam.update();
    }

    @Override
    public void render(Vector3 vector3, Camera camera, Matrix4 matrix4, boolean useAmbientLighting, boolean applyFog) {
        if (this.modelInstance == null) return;
        tmpMat4.set(matrix4);
        modelInstance.transform.set(tmpMat4);
        SeamlessPortalsRenderUtil.renderModel(modelInstance, camera, vector3, useAmbientLighting, applyFog);
    }

    @Override
    public void dispose(WeakReference<Item> weakReference) {}

    @Override
    public Camera getItemSlotCamera() {
        return itemCam;
    }

    @Override
    public void renderAsHeldItem(Vector3 worldPosition, Camera worldCamera, float popUpTimer, float maxPopUpTimer, float swingTimer, float maxSwingTimer) {
        heldItemCamera.fieldOfView = 50.0F;
        heldItemCamera.viewportHeight = worldCamera.viewportHeight;
        heldItemCamera.viewportWidth = worldCamera.viewportWidth;
        heldItemCamera.near = worldCamera.near;
        heldItemCamera.far = worldCamera.far;
        heldItemCamera.update();

        Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
        if (viewModelInstance != null){
            tmpMat4.set(heldModelMatrix);
            viewModelInstance.transform.set(tmpMat4);
            SeamlessPortalsRenderUtil.renderModel(viewModelInstance, heldItemCamera, worldPosition, true, true);
        }
        else{
            this.render(worldPosition, heldItemCamera, heldModelMatrix, true, true);
        }
    }

    @Override
    public void renderAsItemEntity(Vector3 worldPosition, Camera worldCamera, Matrix4 modelMat) {
        tmpMat4.set(modelMat);
        tmpMat4.mul(this.onGroundModelMatrix);
        this.render(worldPosition, worldCamera, tmpMat4, true, true);
    }

    static {
        setupItemCam();
        heldItemCamera = new PerspectiveCamera();
    }

    @Override
    public void renderAsSlicedEntity(Vector3 position, Camera renderCamera, Matrix4 modelMatrix, Portal portal, boolean isDuplicate) {
        tmpMat4.set(modelMatrix);
        tmpMat4.mul(this.onGroundModelMatrix);
        if (this.modelInstance == null) return;
        modelInstance.transform.set(tmpMat4);
        SeamlessPortalsRenderUtil.renderModelSliced(modelInstance, renderCamera, position, portal, isDuplicate);
    }
}
