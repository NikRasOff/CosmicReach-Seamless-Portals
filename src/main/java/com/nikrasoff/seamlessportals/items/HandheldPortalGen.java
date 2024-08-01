package com.nikrasoff.seamlessportals.items;

import com.nikrasoff.seamlessportals.extras.IModEntityModel;
import com.nikrasoff.seamlessportals.mixin.EntityModelInstanceMixin;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.rendering.entities.EntityModel;

public class HandheldPortalGen implements Item {
    public static Entity dummyEntity = new Entity("seamlessportals:dummy");
    public static EntityModel hpgEntityModel = (EntityModel) EntityModel.load(dummyEntity, "handheld_portal_gen.json", "handheld_portal_gen.anim.json",
            "animation.handheld_portal_generator.idle", "handheld_portal_gen.png");

    public static String currentAnimation = "none";

    public static void resetAnimationTimer(){
        ((EntityModelInstanceMixin) hpgEntityModel.getModelInstance(dummyEntity)).setAnimTimer(0);
    }

    public static boolean isAnimOver(String animName, float time){
        if (!currentAnimation.equals(animName)){
            return false;
        }
        return isAnimOver(time);
    }

    public static boolean isAnimOver(float time){
        return ((EntityModelInstanceMixin) hpgEntityModel.getModelInstance(dummyEntity)).getAnimTimer() >= time;
    }

    @Override
    public String getID() {
        return "seamlessportals:handheld_portal_generator";
    }

    @Override
    public boolean canMergeWith(Item item) {
        return item instanceof HandheldPortalGen;
    }

    @Override
    public boolean canMergeWithSwapGroup(Item item) {
        return false;
    }
}
