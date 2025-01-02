package com.nikrasoff.seamlessportals.tooltips;

import com.github.puzzle.game.items.data.DataTagManifest;
import com.github.puzzle.game.util.DataTagUtil;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.items.SyncedOmniumCrystal;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.lang.Lang;
import me.nabdev.cosmictooltips.api.ITooltipItem;

public class CalibratedOmniumTooltip implements ITooltipItem {
    @Override
    public String getItemID() {
        return SyncedOmniumCrystal.CALIBRATED_OMNIUM_ID.toString();
    }

    @Override
    public String getTooltipText(ItemStack itemStack) {
        DataTagManifest manifest = DataTagUtil.getManifestFromStack(itemStack);
        if (manifest.hasTag("frequency")) {
            return Lang.get("seamlessportals:calibrated_omnium_crystal.frequency") + manifest.getTag("frequency").getValue();
        }
        return Lang.get("seamlessportals:calibrated_omnium_crystal.frequency") + "???";
    }
}
