package com.nikrasoff.seamlessportals.items;

import finalforeach.cosmicreach.items.Item;

public class HandheldPortalGen implements Item {
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
