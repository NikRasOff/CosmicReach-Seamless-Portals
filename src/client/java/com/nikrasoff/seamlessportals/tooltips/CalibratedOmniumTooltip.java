package com.nikrasoff.seamlessportals.tooltips;

import com.nikrasoff.seamlessportals.items.SyncedOmniumCrystal;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.lang.Lang;
import io.github.puzzle.cosmic.api.util.DataPointUtil;
import io.github.puzzle.cosmic.impl.data.point.DataPointManifest;
import me.nabdev.cosmictooltips.api.ITooltipItem;

public class CalibratedOmniumTooltip implements ITooltipItem {
    @Override
    public String getItemID() {
        return SyncedOmniumCrystal.CALIBRATED_OMNIUM_ID.toString();
    }

    @Override
    public String getTooltipText(ItemStack itemStack) {
        DataPointManifest manifest = (DataPointManifest) DataPointUtil.getManifestFromStack(itemStack);
        if (manifest.has("frequency")) {
            return Lang.get("seamlessportals:calibrated_omnium_crystal.frequency") + manifest.get("frequency").getValue();
        }
        return Lang.get("seamlessportals:calibrated_omnium_crystal.frequency") + "???";
    }
}
