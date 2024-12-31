package com.nikrasoff.seamlessportals.items;

import com.github.puzzle.game.items.IModItem;
import com.github.puzzle.game.items.data.DataTagManifest;
import com.github.puzzle.game.util.DataTagUtil;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.lang.Lang;
import finalforeach.cosmicreach.util.Identifier;

import static com.nikrasoff.seamlessportals.SeamlessPortalsConstants.MOD_ID;

public class SyncedOmniumCrystal extends AnimatedItem {
    public static Identifier CALIBRATED_OMNIUM_ID = Identifier.of(MOD_ID, "calibrated_omnium_crystal");


    public SyncedOmniumCrystal(){
        super(8, 4, "omnium_crystal_calibrated");
    }

    @Override
    public Identifier getIdentifier() {
        return CALIBRATED_OMNIUM_ID;
    }

    @Override
    public String getName() {
        return Lang.get(CALIBRATED_OMNIUM_ID.toString());
    }

    public String getName(ItemStack stack){
        DataTagManifest manifest = DataTagUtil.getManifestFromStack(stack);
        if (manifest.hasTag("frequency")) {
            return Lang.get("seamlessportals:calibrated_omnium_crystal.frequency") + manifest.getTag("frequency").getValue() + "\n" + Lang.get(CALIBRATED_OMNIUM_ID.toString());
        }
        return this.getName();
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
