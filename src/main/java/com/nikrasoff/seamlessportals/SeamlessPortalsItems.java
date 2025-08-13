package com.nikrasoff.seamlessportals;

import com.nikrasoff.seamlessportals.items.HandheldPortalGen;
import com.nikrasoff.seamlessportals.items.SyncedOmniumCrystal;
import com.nikrasoff.seamlessportals.items.UnstableHandheldPortalGen;
import com.nikrasoff.seamlessportals.items.WarpCore;
import finalforeach.cosmicreach.GameAssetLoader;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.items.ItemThing;
import finalforeach.cosmicreach.util.Identifier;
import io.github.puzzle.cosmic.item.AbstractCosmicItem;

public class SeamlessPortalsItems {
    public static Item OMNIUM_CRYSTAL;
    public static SyncedOmniumCrystal CALIBRATED_OMNIUM_CRYSTAL;

    public static void registerItems(){
        SeamlessPortals.LOGGER.info("Registering items from Seamless Portals!");
        String[] itemIds = {
                "omnium_crystal",
                "laser_emitter"
        };

        for (String id : itemIds){
            ItemThing.loadItemFromJson(GameAssetLoader.loadJson(GameAssetLoader.loadAsset(Identifier.of(SeamlessPortalsConstants.MOD_ID, "items/" + id + ".json"))));
        }
        OMNIUM_CRYSTAL = Item.getItem("seamlessportals:omnium_crystal");

        CALIBRATED_OMNIUM_CRYSTAL = new SyncedOmniumCrystal();
        AbstractCosmicItem.register(CALIBRATED_OMNIUM_CRYSTAL);
        AbstractCosmicItem.register(new HandheldPortalGen());
        AbstractCosmicItem.register(new WarpCore());
        AbstractCosmicItem.register(new UnstableHandheldPortalGen());
    }
}
