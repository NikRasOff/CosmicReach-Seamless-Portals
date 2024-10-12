package com.nikrasoff.seamlessportals.items;

import com.badlogic.gdx.math.Vector3;
import com.nikrasoff.seamlessportals.extras.interfaces.CustomPropertyItem;
import finalforeach.cosmicreach.io.CRBinDeserializer;
import finalforeach.cosmicreach.io.CRBinSerializer;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.lang.Lang;

import java.util.Map;

public class HandheldPortalGen implements Item, CustomPropertyItem {
    public static final String hpgID = "seamlessportals:handheld_portal_generator";

    @Override
    public String getID() {
        return hpgID;
    }

    @Override
    public boolean canMergeWith(Item item) {
        return item instanceof HandheldPortalGen;
    }

    @Override
    public boolean canMergeWithSwapGroup(Item item) {
        return false;
    }

    @Override
    public boolean isCatalogHidden() {
        return false;
    }

    @Override
    public boolean hasIntProperty(String s) {
        return false;
    }

    @Override
    public int getIntProperty(String s, int i) {
        return 0;
    }

    @Override
    public boolean hasTag(String s) {
        return false;
    }

    @Override
    public String getName() {
        return Lang.get(hpgID);
    }

    @Override
    public int getDefaultStackLimit() {
        return 1;
    }

    @Override
    public void readCustomProperties(CRBinDeserializer crbd, Map<String, Object> customProperties) {
        customProperties.put("portal1Chunk", new Vector3(crbd.readFloat("portal1x", 0), crbd.readFloat("portal1y", 0), crbd.readFloat("portal1z", 0)));
        customProperties.put("portal1Id", crbd.readInt("portal1Id", -1));
        customProperties.put("portal2Chunk", new Vector3(crbd.readFloat("portal2x", 0), crbd.readFloat("portal2y", 0), crbd.readFloat("portal2z", 0)));
        customProperties.put("portal2Id", crbd.readInt("portal2Id", -1));
    }

    @Override
    public void writeCustomProperties(CRBinSerializer crbs, Map<String, Object> customProperties) {
        Vector3 p1c = (Vector3) customProperties.getOrDefault("portal1Chunk", new Vector3());
        crbs.writeFloat("portal1x", p1c.x);
        crbs.writeFloat("portal1y", p1c.y);
        crbs.writeFloat("portal1z", p1c.z);
        int p1id = (int) customProperties.getOrDefault("portal1Id", -1);
        crbs.writeInt("portal1Id", p1id);
        Vector3 p2c = (Vector3) customProperties.getOrDefault("portal2Chunk", new Vector3());
        crbs.writeFloat("portal2x", p2c.x);
        crbs.writeFloat("portal2y", p2c.y);
        crbs.writeFloat("portal2z", p2c.z);
        int p2id = (int) customProperties.getOrDefault("portal2Id", -1);
        crbs.writeInt("portal2Id", p2id);
    }
}
