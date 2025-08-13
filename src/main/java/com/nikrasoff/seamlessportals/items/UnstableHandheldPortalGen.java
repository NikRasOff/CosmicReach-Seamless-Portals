package com.nikrasoff.seamlessportals.items;

import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.lang.Lang;
import finalforeach.cosmicreach.util.Identifier;
import io.github.puzzle.cosmic.item.AbstractCosmicItem;

public class UnstableHandheldPortalGen extends AbstractCosmicItem {
    public static final String hpgID = "seamlessportals:unstable_handheld_portal_generator";

    public UnstableHandheldPortalGen() {
        super(Identifier.of(hpgID));
    }

    @Override
    public String getID() {
        return hpgID;
    }

    @Override
    public boolean canMergeWith(Item item) {
        return item instanceof UnstableHandheldPortalGen;
    }

    @Override
    public boolean canMergeWithSwapGroup(Item item) {
        return false;
    }

    @Override
    public String getName() {
        return Lang.get(hpgID);
    }

    @Override
    public int getDefaultStackLimit() {
        return 1;
    }
}
