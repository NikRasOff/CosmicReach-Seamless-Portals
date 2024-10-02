package com.nikrasoff.seamlessportals.rendering.models;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.nikrasoff.seamlessportals.extras.interfaces.IModEntityModelInstance;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.rendering.entities.EntityModelInstance;
import finalforeach.cosmicreach.rendering.items.ItemModel;
import finalforeach.cosmicreach.rendering.entities.EntityModel;

import java.lang.ref.WeakReference;

public class EntityItemModel extends ItemModel {
    public static Array<IModEntityModelInstance> activeModels = new Array<>();
    public static Entity dummyEntity = new Entity("seamlessportals:dummy");
    public EntityModel entityModel;
    public EntityModelInstance entityModelInstance;
    public static Camera itemCam;
    public Matrix4 heldModelMatrix = new Matrix4();
    public Matrix4 onGroundModelMatrix = new Matrix4();

    private static Matrix4 tmpMat4 = new Matrix4();

    private static final PerspectiveCamera heldItemCamera;

    private static Json json = new Json();

    public EntityItemModel(String modelFileName, String animationSetName, String defaultAnimName, String textureName){
        this.entityModel = (EntityModel) EntityModel.load(dummyEntity, modelFileName, animationSetName, defaultAnimName, textureName);
        this.entityModelInstance = (EntityModelInstance) this.entityModel.getNewModelInstance();
        activeModels.add((IModEntityModelInstance) this.entityModelInstance);
    }

    private static void setupItemCam() {
        itemCam = new OrthographicCamera(100.0F, 100.0F);
        itemCam.position.set(50.0F, 50.0F, 50.0F);
        itemCam.lookAt(0.5F, 0.5F, 0.5F);
        ((OrthographicCamera)itemCam).zoom = 0.017F;
        itemCam.update();
    }

    public static void advanceAnimations(){
        for (IModEntityModelInstance model : activeModels){
            model.updateAnimation(dummyEntity);
        }
    }

    @Override
    public void render(Vector3 vector3, Camera camera, Matrix4 matrix4, boolean b) {
        ((IModEntityModelInstance) this.entityModelInstance).renderNoAnim(dummyEntity, camera, matrix4);
    }

    @Override
    public void dispose(WeakReference<Item> weakReference) {
        activeModels.removeValue((IModEntityModelInstance) this.entityModelInstance, true);
        this.entityModel = null;
        this.entityModelInstance = null;
    }

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
//        identMat4.idt();
//
//        identMat4.scale(0.5F, 0.5F, 0.5F);
//        identMat4.translate(0.4F, -0.55F, -1.75F);
//        identMat4.rotate(Vector3.Y, 175F);
//        identMat4.translate(-0.25F, -0.25F, -0.25F);

//                Gdx.gl.glDisable(2929);
//        this.entityModel.render(InGame.getLocalPlayer().getEntity(), heldItemCamera, heldModelMatrix);
//                Gdx.gl.glEnable(2929);
    }

    @Override
    public void renderAsItemEntity(Vector3 worldPosition, Camera worldCamera, Matrix4 modelMat) {
        tmpMat4.set(this.onGroundModelMatrix);
        tmpMat4.mul(modelMat);
        this.render(worldPosition, worldCamera, tmpMat4, true);
    }

    static {
        setupItemCam();
        heldItemCamera = new PerspectiveCamera();
    }
}
