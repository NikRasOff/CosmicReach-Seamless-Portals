package com.nikrasoff.seamlessportals.items;

import finalforeach.cosmicreach.lang.Lang;
import finalforeach.cosmicreach.util.Identifier;

import static com.nikrasoff.seamlessportals.SeamlessPortalsConstants.MOD_ID;

public class WarpCore extends AnimatedItem {
    public static Identifier WARP_CORE_ID = Identifier.of(MOD_ID, "warp_core");

    public WarpCore(){
        super(8, 4, "warp_core", WARP_CORE_ID);
    }

    @Override
    public String getName() {
        return Lang.get(WARP_CORE_ID.toString());
    }
}
