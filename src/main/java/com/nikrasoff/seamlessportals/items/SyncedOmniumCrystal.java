package com.nikrasoff.seamlessportals.items;

import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.lang.Lang;
import finalforeach.cosmicreach.util.Identifier;
import io.github.puzzle.cosmic.api.util.DataPointUtil;
import io.github.puzzle.cosmic.impl.data.point.DataPointManifest;

import static com.nikrasoff.seamlessportals.SeamlessPortalsConstants.MOD_ID;

public class SyncedOmniumCrystal extends AnimatedItem {
    public static Identifier CALIBRATED_OMNIUM_ID = Identifier.of(MOD_ID, "calibrated_omnium_crystal");


    public SyncedOmniumCrystal(){
        super(8, 4, "omnium_crystal_calibrated", CALIBRATED_OMNIUM_ID);
    }

    @Override
    public String getName() {
        return Lang.get(CALIBRATED_OMNIUM_ID.toString());
    }

    public String getName(ItemStack stack){
        DataPointManifest manifest = (DataPointManifest) DataPointUtil.getManifestFromStack(stack);
        if (manifest.has("frequency")) {
            return Lang.get("seamlessportals:calibrated_omnium_crystal.frequency") + manifest.get("frequency").getValue() + "\n" + Lang.get(CALIBRATED_OMNIUM_ID.toString());
        }
        return this.getName();
    }

    @Override
    public int getDefaultStackLimit() {
        return 1;
    }

    @Override
    public boolean isCatalogHidden() {
        return true;
    }
}
