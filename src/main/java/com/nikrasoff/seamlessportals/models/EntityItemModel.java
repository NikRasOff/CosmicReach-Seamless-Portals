package com.nikrasoff.seamlessportals.models;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.items.ItemModel;
import finalforeach.cosmicreach.rendering.entities.EntityModel;

import java.lang.ref.WeakReference;

public class EntityItemModel extends ItemModel {
    public EntityModel entityModel;
    public EntityItemModel(String modelFileName, String animationSetName, String defaultAnimName, String textureName){
        this.entityModel = (EntityModel) EntityModel.load(modelFileName, animationSetName, defaultAnimName, textureName);
    }

    @Override
    public void render(Camera camera) {
        this.entityModel.render(InGame.getLocalPlayer().getEntity(), camera, new Matrix4());
    }

    @Override
    public void dispose(WeakReference<Item> weakReference) {
        this.entityModel = null;
    }
}
