package com.nikrasoff.seamlessportals.models;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.nikrasoff.seamlessportals.extras.IModEntityModel;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.items.ItemModel;
import finalforeach.cosmicreach.rendering.entities.EntityModel;

import java.lang.ref.WeakReference;

public class EntityItemModel extends ItemModel {
    public static Array<IModEntityModel> activeModels = new Array<>();
    public static Entity dummyEntity = new Entity("seamlessportals:dummy");
    public EntityModel entityModel;
    public EntityItemModel(String modelFileName, String animationSetName, String defaultAnimName, String textureName){
        this.entityModel = (EntityModel) EntityModel.load(dummyEntity, modelFileName, animationSetName, defaultAnimName, textureName);
        activeModels.add((IModEntityModel) this.entityModel);
    }

    public static void advanceAnimations(){
        for (IModEntityModel model : activeModels){
            model.updateAnimation(dummyEntity);
        }
    }

    @Override
    public void render(Camera camera, Matrix4 modelMat) {
        ((IModEntityModel) this.entityModel).renderNoAnim(dummyEntity, camera, new Matrix4());
    }

    @Override
    public void dispose(WeakReference<Item> weakReference) {
        activeModels.removeValue((IModEntityModel) this.entityModel, true);
        this.entityModel = null;
    }
}
