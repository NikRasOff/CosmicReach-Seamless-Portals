package com.nikrasoff.seamlessportals.entities;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.SeamlessPortalsConstants;
import com.nikrasoff.seamlessportals.entities.components.PortalCheckComponent;
import com.nikrasoff.seamlessportals.networking.packets.CreateEffectPacket;
import finalforeach.cosmicreach.singletons.GameSingletons;
import finalforeach.cosmicreach.entities.CommonEntityTags;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.components.GravityComponent;
import finalforeach.cosmicreach.networking.server.ServerSingletons;
import finalforeach.cosmicreach.savelib.crbin.CRBSerialized;
import finalforeach.cosmicreach.savelib.crbin.CRBinSerializer;
import finalforeach.cosmicreach.util.Identifier;
import finalforeach.cosmicreach.world.Zone;

import java.util.HashMap;
import java.util.Map;

public class DestabiliserPulseEntity extends Entity {
    public static final Identifier ENTITY_ID = Identifier.of(SeamlessPortalsConstants.MOD_ID, "destabiliser_pulse");
    @CRBSerialized
    float destroyRadius;

    public DestabiliserPulseEntity() {
        super(ENTITY_ID.toString());
        this.addTag(CommonEntityTags.NO_DESPAWN);
        this.addTag(CommonEntityTags.NOCLIP);
        this.addTag(CommonEntityTags.PROJECTILE_IMMUNE);
        this.addTag(CommonEntityTags.NO_ENTITY_PUSH);
        this.addTag(CommonEntityTags.NO_BUOYANCY);
        this.removeUpdatingComponent(GravityComponent.INSTANCE);
        this.removeUpdatingComponent(PortalCheckComponent.INSTANCE);
    }

    public void prepareForSpawn(float radius, Vector3 pos, Zone zone){
        this.destroyRadius = radius;
        this.setPosition(pos);
        if (GameSingletons.isClient){
            Map<String, Object> argMap = new HashMap<>();
            argMap.put("radius", radius);
            SeamlessPortals.effectManager.createEffect(ENTITY_ID, 0, pos, zone, argMap);
        }
        if (GameSingletons.isHost && ServerSingletons.SERVER != null){
            CRBinSerializer arguments = new CRBinSerializer();
            arguments.writeFloat("radius", radius);
            ServerSingletons.SERVER.broadcast(zone, new CreateEffectPacket(ENTITY_ID, 0, pos, zone, arguments));
        }
    }

    @Override
    public void update(Zone zone, float deltaTime) {
        super.update(zone, deltaTime);
        if (this.age >= 1.5f){
            SeamlessPortals.portalManager.createdPortals.forEach((portalID, portal) -> {
                Vector3 portalPos = portal.position.cpy();

                Vector3 destroyDiff = new Vector3(this.destroyRadius, this.destroyRadius, this.destroyRadius);
                Vector3 destroyMin = this.position.cpy().sub(destroyDiff);
                Vector3 destroyMax = this.position.cpy().add(destroyDiff);
                BoundingBox destroyBounds = new BoundingBox(destroyMin, destroyMax);
                if (destroyBounds.contains(portalPos)){
                    portal.startDestruction();
                    if (portal.linkedPortal != null){
                        portal.linkedPortal.startDestruction();
                    }
                }
            });
            this.onDeath();
        }
    }
}
