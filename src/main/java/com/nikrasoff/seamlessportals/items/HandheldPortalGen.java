package com.nikrasoff.seamlessportals.items;

import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.lang.Lang;

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
        return Lang.get(hpgID);
    }
}
