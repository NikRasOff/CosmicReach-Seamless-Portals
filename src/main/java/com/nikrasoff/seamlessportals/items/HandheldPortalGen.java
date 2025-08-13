package com.nikrasoff.seamlessportals.items;

import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.lang.Lang;
import finalforeach.cosmicreach.sounds.GameSound;
import finalforeach.cosmicreach.util.Identifier;
import io.github.puzzle.cosmic.item.AbstractCosmicItem;

public class HandheldPortalGen extends AbstractCosmicItem {
    public static final String hpgID = "seamlessportals:handheld_portal_generator";
    public static final GameSound hpgFireSound = GameSound.of("seamlessportals:sounds/portals/hpg_fire.ogg");

    public HandheldPortalGen() {
        super(Identifier.of(hpgID));
    }

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
    public String getName() {
        return Lang.get(hpgID);
    }

    @Override
    public int getDefaultStackLimit() {
        return 1;
    }
}
