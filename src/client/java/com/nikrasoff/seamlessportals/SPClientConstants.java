package com.nikrasoff.seamlessportals;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ObjectMap;
import com.nikrasoff.seamlessportals.extras.interfaces.IModEntityModelInstance;
import com.nikrasoff.seamlessportals.rendering.portal_entity_renderers.DefaultPortalEntityRenderer;
import com.nikrasoff.seamlessportals.api.IPortalEntityRenderer;
import com.nikrasoff.seamlessportals.extras.interfaces.IPortalIngame;
import com.nikrasoff.seamlessportals.extras.interfaces.IPortalablePlayerController;
import com.nikrasoff.seamlessportals.portals.Portal;
import com.nikrasoff.seamlessportals.rendering.models.PortalModel;
import com.nikrasoff.seamlessportals.rendering.portal_entity_renderers.PortalPortalEntityRenderer;
import finalforeach.cosmicreach.GameAssetLoader;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.rendering.GameTexture;
import finalforeach.cosmicreach.rendering.entities.IEntityModelInstance;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import finalforeach.cosmicreach.util.Identifier;

import static com.nikrasoff.seamlessportals.SeamlessPortalsConstants.MOD_ID;

public class SPClientConstants implements ISPClientConstants {
    private static ObjectMap<Class<? extends Entity>, IPortalEntityRenderer> entityRendererMap;
    private static IPortalEntityRenderer defaultRenderer;
    @Override
    public IEntityModelInstance getNewPortalModelInstance() {
        return PortalModel.model.getNewModelInstance();
    }

    @Override
    public IEntityModelInstance getNewPulseModelInstance() {
        return null;
    }

    @Override
    public void animateCameraTurning(Vector3 originalPos, Vector3 newPos, Portal portal) {
        IPortalablePlayerController locPlayer = (IPortalablePlayerController) ((IPortalIngame) GameState.IN_GAME).getPlayerController();
        Vector3 offset = originalPos.sub(newPos);
        locPlayer.cosmicReach_Seamless_Portals$flagForCameraTeleport(portal);
        locPlayer.cosmicReach_Seamless_Portals$portalCurrentCameraTransform(portal, offset);
    }

    @Override
    public void flagEntityModelInstanceForTeleporting(Entity entity, Portal portal) {
        if (entity.modelInstance == null) return;
        if (entity.modelInstance instanceof IModEntityModelInstance instance){
            instance.cosmicReach_Seamless_Portals$flagForTeleporting(portal);
        }
    }

    public static GameTexture UI_LASER_RIGHT_ON;
    public static GameTexture UI_LASER_RIGHT_OFF;
    public static GameTexture UI_LASER_LEFT_ON;
    public static GameTexture UI_LASER_LEFT_OFF;
    public static GameTexture UI_LASER_WHOLE_ON;
    public static GameTexture UI_LASER_WHOLE_OFF;
    public static GameTexture UI_ARROW_OMNIUM_CALIBRATOR;
    public static GameTexture UI_TEXT_CURSOR;
    public static GameTexture UI_PORTAL_GEN_ICON;
    public static GameTexture UI_SPACIAL_ANCHOR_ICON;
    public static GameShader OVERRIDE_ITEM_SHADER;

    public static void init(){
        entityRendererMap = new ObjectMap<>();
        defaultRenderer = new DefaultPortalEntityRenderer();
        UI_LASER_RIGHT_ON = GameTexture.load(Identifier.of(MOD_ID, "textures/ui/laser_right_on.png").toString());
        UI_LASER_RIGHT_OFF = GameTexture.load(Identifier.of(MOD_ID, "textures/ui/laser_right_off.png").toString());
        UI_LASER_LEFT_ON = GameTexture.load(Identifier.of(MOD_ID, "textures/ui/laser_left_on.png").toString());
        UI_LASER_LEFT_OFF = GameTexture.load(Identifier.of(MOD_ID, "textures/ui/laser_left_off.png").toString());
        UI_LASER_WHOLE_ON = GameTexture.load(Identifier.of(MOD_ID, "textures/ui/laser_whole_on.png").toString());
        UI_LASER_WHOLE_OFF = GameTexture.load(Identifier.of(MOD_ID, "textures/ui/laser_whole_off.png").toString());
        UI_ARROW_OMNIUM_CALIBRATOR = GameTexture.load(Identifier.of(MOD_ID, "textures/ui/arrow_omnium_calibrator.png").toString());
        UI_TEXT_CURSOR = GameTexture.load(Identifier.of(MOD_ID, "textures/ui/text_cursor.png").toString());
        UI_PORTAL_GEN_ICON = GameTexture.load(Identifier.of(MOD_ID, "textures/ui/portal_generator_icon.png").toString());
        UI_SPACIAL_ANCHOR_ICON = GameTexture.load(Identifier.of(MOD_ID, "textures/ui/spacial_anchor_icon.png").toString());
        registerPortalEntityRenderer(Portal.class, new PortalPortalEntityRenderer());
        OVERRIDE_ITEM_SHADER = new GameShader(Identifier.of(MOD_ID, "shaders/override/item_shader.vert.glsl"), Identifier.of(MOD_ID, "shaders/override/item_shader.frag.glsl"));
    }

    public static void registerPortalEntityRenderer(Class<? extends Entity> clazz, IPortalEntityRenderer entityRenderer){
        if (entityRendererMap == null){
            throw new RuntimeException("You tried to register an entity renderer before the entity renderer map was even created");
        }
        if (entityRendererMap.containsKey(clazz)){
            throw new RuntimeException("Cannot have multiple entity renderers registered to the same entity class: " + clazz.getSimpleName());
        }
        entityRendererMap.put(clazz, entityRenderer);
    }

    public static IPortalEntityRenderer getPortalEntityRenderer(Class<? extends Entity> clazz){
        if (entityRendererMap == null){
            return null;
        }
        Class<? extends Entity> cl = clazz;
        for(IPortalEntityRenderer r; cl != null; cl = (Class<? extends Entity>) cl.getSuperclass()) {
            r = entityRendererMap.get(cl);
            if (r != null) {
                return r;
            }
        }
        return defaultRenderer;
    }


}
