package com.nikrasoff.seamlessportals.items;

import com.github.puzzle.game.items.IModItem;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.lang.Lang;
import finalforeach.cosmicreach.sounds.GameSound;
import finalforeach.cosmicreach.util.Identifier;

public class HandheldPortalGen implements IModItem {
    public static final String hpgID = "seamlessportals:handheld_portal_generator";
    public static final GameSound hpgFireSound = GameSound.of("seamlessportals:sounds/portals/hpg_fire.ogg");

    @Override
    public Identifier getIdentifier() {
        return Identifier.of(hpgID);
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

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
