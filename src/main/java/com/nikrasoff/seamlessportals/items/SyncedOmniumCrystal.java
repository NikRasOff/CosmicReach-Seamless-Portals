package com.nikrasoff.seamlessportals.items;

import com.github.puzzle.game.items.IModItem;
import com.github.puzzle.game.items.data.DataTagManifest;
import finalforeach.cosmicreach.lang.Lang;
import finalforeach.cosmicreach.util.Identifier;

import static com.nikrasoff.seamlessportals.SeamlessPortalsConstants.CALIBRATED_OMNIUM_ID;
import static com.nikrasoff.seamlessportals.SeamlessPortalsConstants.MOD_ID;

public class SyncedOmniumCrystal implements IModItem {
    public final DataTagManifest tagManifest = new DataTagManifest();

    public SyncedOmniumCrystal(){
        this.addTexture(IModItem.MODEL_2_5D_ITEM, Identifier.of(MOD_ID, "omnium_crystal_calibrated.png"));
    }

    @Override
    public DataTagManifest getTagManifest() {
        return tagManifest;
    }

    @Override
    public Identifier getIdentifier() {
        return CALIBRATED_OMNIUM_ID;
    }

    @Override
    public String getName() {
        return Lang.get(CALIBRATED_OMNIUM_ID.toString());
    }

    @Override
    public int getMaxStackSize() {
        return 2;
    }

    @Override
    public boolean isCatalogHidden() {
        return true;
    }
}
