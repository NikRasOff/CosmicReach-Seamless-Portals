package com.nikrasoff.seamlessportals;

import com.nikrasoff.seamlessportals.tooltips.CalibratedOmniumTooltip;
import me.nabdev.cosmictooltips.CosmicToolTips;
import me.nabdev.cosmictooltips.api.ToolTipFactory;
import me.nabdev.cosmictooltips.utils.TooltipUtils;

public class SeamlessPortalsTooltips extends ToolTipFactory {
    public SeamlessPortalsTooltips(){
        SeamlessPortals.LOGGER.info("We tooltipping");
        addTooltip(new CalibratedOmniumTooltip());
    }

    public boolean areAdvancedTooltipsOn(){
        return TooltipUtils.shouldBeAdvanced();
    }
}
