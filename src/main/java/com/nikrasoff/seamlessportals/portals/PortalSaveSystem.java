package com.nikrasoff.seamlessportals.portals;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import finalforeach.cosmicreach.io.SaveLocation;
import finalforeach.cosmicreach.world.World;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class PortalSaveSystem {
    public static String localPortalFileName = "seamlessportals/portal_data.json";

    public static void savePortals(World world) {
        if (SeamlessPortals.portalManager != null) {
            String worldFolder = SaveLocation.getWorldSaveFolderLocation(world);
            File file = new File(worldFolder + "/" + localPortalFileName);

            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (Exception var9) {
                var9.printStackTrace();
            }

            try {
                FileOutputStream fos = new FileOutputStream(file);

                try {
                    Json json = new Json();
                    json.setOutputType(JsonWriter.OutputType.json);
                    String jsonStr = json.prettyPrint(SeamlessPortals.portalManager);
                    fos.write(jsonStr.getBytes());
                } catch (Throwable var7) {
                    try {
                        fos.close();
                    } catch (Throwable var6) {
                        var7.addSuppressed(var6);
                    }

                    throw var7;
                }

                fos.close();
            } catch (Exception var8) {
                var8.printStackTrace();
            }

        }
    }

    public static void loadPortals(World world) {
        String worldFolder = SaveLocation.getWorldSaveFolderLocation(world);
        File file = new File(worldFolder + "/" + localPortalFileName);
        if (file.exists()) {
            try {
                FileInputStream fis = new FileInputStream(file);

                try {
                    Json json = new Json();
                    json.setIgnoreUnknownFields(true);
                    SeamlessPortals.portalManager = json.fromJson(PortalManager.class, fis);
                } catch (Throwable var7) {
                    try {
                        fis.close();
                    } catch (Throwable var6) {
                        var7.addSuppressed(var6);
                    }

                    throw var7;
                }

                fis.close();
            } catch (Exception var8) {
                var8.printStackTrace();
            }

        }
    }
}
