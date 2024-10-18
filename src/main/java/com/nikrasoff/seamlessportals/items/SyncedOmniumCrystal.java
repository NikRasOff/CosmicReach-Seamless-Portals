package com.nikrasoff.seamlessportals.items;

import com.github.puzzle.game.items.IModItem;
import com.github.puzzle.game.items.data.DataTagManifest;
import finalforeach.cosmicreach.lang.Lang;
import finalforeach.cosmicreach.util.Identifier;

import static com.nikrasoff.seamlessportals.SeamlessPortals.MOD_ID;

public class SyncedOmniumCrystal implements IModItem {
    public static final Identifier socID = Identifier.of(MOD_ID, "synced_omnium_crystal");
    public final DataTagManifest tagManifest = new DataTagManifest();

    public SyncedOmniumCrystal(){
        this.addTexture(IModItem.MODEL_2_5D_ITEM, Identifier.of(MOD_ID, "omnium_crystal_linked.png"));
    }

    @Override
    public DataTagManifest getTagManifest() {
        return tagManifest;
    }

    @Override
    public Identifier getIdentifier() {
        return socID;
    }

    @Override
    public String getName() {
        return Lang.get(socID.toString());
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean isCatalogHidden() {
        return true;
    }
}
