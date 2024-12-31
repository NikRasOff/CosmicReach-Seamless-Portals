package com.nikrasoff.seamlessportals;

import com.github.puzzle.game.items.IModItem;
import com.nikrasoff.seamlessportals.items.HandheldPortalGen;
import com.nikrasoff.seamlessportals.items.SyncedOmniumCrystal;
import com.nikrasoff.seamlessportals.items.UnstableHandheldPortalGen;
import finalforeach.cosmicreach.GameAssetLoader;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.items.ItemThing;
import finalforeach.cosmicreach.util.Identifier;

public class SeamlessPortalsItems {
    public static Item OMNIUM_CRYSTAL;
    public static SyncedOmniumCrystal CALIBRATED_OMNIUM_CRYSTAL;

    public static void registerItems(){
        SeamlessPortals.LOGGER.info("Registering items from Seamless Portals!");
        String[] itemIds = {
                "omnium_crystal"
        };

        for (String id : itemIds){
            ItemThing.loadItemFromJson(GameAssetLoader.loadJson(GameAssetLoader.loadAsset(Identifier.of(SeamlessPortalsConstants.MOD_ID, "items/" + id + ".json"))));
        }
        OMNIUM_CRYSTAL = Item.getItem("seamlessportals:omnium_crystal");

        CALIBRATED_OMNIUM_CRYSTAL = new SyncedOmniumCrystal();
        IModItem.registerItem(CALIBRATED_OMNIUM_CRYSTAL);
        Item.registerItem(new HandheldPortalGen());
        Item.registerItem(new UnstableHandheldPortalGen());
    }
}
