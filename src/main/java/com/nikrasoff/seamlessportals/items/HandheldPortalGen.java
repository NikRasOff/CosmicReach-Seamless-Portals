package com.nikrasoff.seamlessportals.items;

import com.nikrasoff.seamlessportals.mixin.EntityModelInstanceMixin;
import com.nikrasoff.seamlessportals.models.EntityItemModel;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.rendering.entities.EntityModel;
import finalforeach.cosmicreach.rendering.items.ItemRenderer;

public class HandheldPortalGen implements Item {
    public static final String hpgID = "seamlessportals:handheld_portal_generator";

    public static String currentAnimation = "none";

    @Override
    public String getID() {
        return hpgID;
    }

    @Override
    public boolean canMergeWith(Item item) {
        return item instanceof HandheldPortalGen;
    }

    @Override
    public boolean canMergeWithSwapGroup(Item item) {
        return false;
    }

    @Override
    public boolean isCatalogHidden() {
        return false;
    }

    @Override
    public boolean hasIntProperty(String s) {
        return false;
    }

    @Override
    public int getIntProperty(String s, int i) {
        return 0;
    }

    @Override
    public boolean hasTag(String s) {
        return false;
    }

    @Override
    public String getName() {
        return null;
    }
}
