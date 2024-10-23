package com.nikrasoff.seamlessportals;

import com.badlogic.gdx.graphics.Texture;
import finalforeach.cosmicreach.GameAssetLoader;
import finalforeach.cosmicreach.util.Identifier;


public class SeamlessPortalsConstants {
    public static String MOD_ID = "seamlessportals";
    public static Identifier CALIBRATED_OMNIUM_ID = Identifier.of(MOD_ID, "calibrated_omnium_crystal");
    public static Identifier OMNIUM_CRYSTAL_ID = Identifier.of(MOD_ID, "omnium_crystal");

    public static Texture UI_LASER_RIGHT_ON;
    public static Texture UI_LASER_LEFT_ON;
    public static Texture UI_LASER_WHOLE_ON;
    public static Texture UI_LASER_WHOLE_OFF;
    public static Texture UI_ARROW_OMNIUM_CALIBRATOR;

    public static void init(){
        UI_LASER_RIGHT_ON = GameAssetLoader.getTexture(Identifier.of(MOD_ID, "textures/ui/laser_right_on.png"));
        UI_LASER_LEFT_ON = GameAssetLoader.getTexture(Identifier.of(MOD_ID, "textures/ui/laser_left_on.png"));
        UI_LASER_WHOLE_ON = GameAssetLoader.getTexture(Identifier.of(MOD_ID, "textures/ui/laser_whole_on.png"));
        UI_LASER_WHOLE_OFF = GameAssetLoader.getTexture(Identifier.of(MOD_ID, "textures/ui/laser_whole_off.png"));
        UI_ARROW_OMNIUM_CALIBRATOR = GameAssetLoader.getTexture(Identifier.of(MOD_ID, "textures/ui/arrow_omnium_calibrator.png"));
    }
}
