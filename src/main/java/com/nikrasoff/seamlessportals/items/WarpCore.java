package com.nikrasoff.seamlessportals.items;

import com.github.puzzle.game.items.IModItem;
import com.github.puzzle.game.items.ITickingItem;
import com.github.puzzle.game.items.data.DataTag;
import com.github.puzzle.game.items.data.DataTagManifest;
import com.github.puzzle.game.items.data.attributes.IntDataAttribute;
import com.github.puzzle.game.util.DataTagUtil;
import finalforeach.cosmicreach.entities.ItemEntity;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.lang.Lang;
import finalforeach.cosmicreach.util.Identifier;
import finalforeach.cosmicreach.world.Zone;

import static com.nikrasoff.seamlessportals.SeamlessPortalsConstants.MOD_ID;

public class WarpCore extends AnimatedItem {
    public static Identifier WARP_CORE_ID = Identifier.of(MOD_ID, "warp_core");

    public WarpCore(){
        super(8, 4, "warp_core");
    }

    @Override
    public Identifier getIdentifier() {
        return WARP_CORE_ID;
    }

    @Override
    public String getName() {
        return Lang.get(WARP_CORE_ID.toString());
    }

    @Override
    public int getMaxStackSize() {
        return 1000;
    }

    @Override
    public boolean isCatalogHidden() {
        return false;
    }
}
