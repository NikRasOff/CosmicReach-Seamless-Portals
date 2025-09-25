package com.nikrasoff.seamlessportals.mixin;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.rendering.entities.instances.EntityModelInstancePlayer;
import finalforeach.cosmicreach.util.GameMath;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityModelInstancePlayer.class)
public abstract class EntityModelInstancePlayerMixin extends EntityModelInstanceMixin{
    @Shadow
    Vector3 usedDir;

    @Shadow
    private Vector3 lastViewDir;

    @Shadow
    private Vector3 tmpVec;

    @Shadow
    private Vector3 tmpVec2;

    @Shadow
    private Vector3 lastUsedDir;

    @Unique
    private Portal cosmicReach_Seamless_Portals$teleportingPortal = null;

    @Override
    protected void cosmicReach_Seamless_Portals$customBoneTransformOverrides(Entity entity) {
        if (entity != null) {
            if (this.rootBone != null) {
                if (!this.tmpVec2.isZero()) {
                    this.setBoneToDirection(this.rootBone, this.tmpVec2);
                }

                if (this.headBone != null) {
                    this.setBoneToDirection(this.headBone, this.lastUsedDir);
                }
            }
        }
    }

    @Override
    public void cosmicReach_Seamless_Portals$updateAnimation(Entity entity, Vector3 renderPos) {
        super.cosmicReach_Seamless_Portals$updateAnimation(entity, renderPos);

        if (this.rootBone != null) {
            Vector3 vel = entity.velocity;
            float velSpeed = vel.dst(0.0F, vel.y, 0.0F);
            if (this.usedDir == null || !this.lastViewDir.epsilonEquals(entity.viewDirection)) {
                this.usedDir = entity.viewDirection;
            }

            if (velSpeed > 0.1F) {
                this.usedDir = entity.velocity;
            }

            if (this.usedDir != null) {
                this.tmpVec.set(this.usedDir);
            }

            this.tmpVec.y = 0.0F;
            this.tmpVec.nor();
            if (this.tmpVec2.isZero()) {
                this.tmpVec2.set(this.tmpVec);
            }

            float lerpTime = Gdx.graphics.getDeltaTime() * Math.min(velSpeed, 4.0F);
            this.tmpVec2.lerp(this.tmpVec, lerpTime);
            GameMath.alignVectorTowardTarget(this.tmpVec2, this.tmpVec, 0.0F);

            lerpTime = Math.min(Gdx.graphics.getDeltaTime() * 16.0F, 1.0F);
            this.lastUsedDir.lerp(this.usedDir, lerpTime);
            this.lastUsedDir.y = 0.0F;
            this.lastUsedDir.nor();
            GameMath.alignVectorTowardTarget(this.lastUsedDir, Vector3.Y, -0.95F);
            GameMath.alignVectorTowardTarget(this.lastUsedDir, this.tmpVec2, 0.0F);
            float yInfluence = MathUtils.clamp(GameMath.norDot(this.lastUsedDir, entity.viewDirection), -0.25F, 0.25F) / 0.25F;
            this.lastUsedDir.y = yInfluence * entity.viewDirection.y;
            this.lastUsedDir.nor();
        }

        this.lastViewDir.set(entity.viewDirection);
    }

    @Override
    public void cosmicReach_Seamless_Portals$flagForTeleporting(Portal portal) {
        // Was supposed to just set a flag
        // But then I decided that this is just better
        this.lastUsedDir.set(portal.getPortaledVector(this.lastUsedDir));
        this.lastUsedDir.y = 0;
        this.lastUsedDir.nor();
        this.lastViewDir.set(portal.getPortaledVector(this.lastViewDir));
        this.tmpVec2.set(portal.getPortaledVector(this.tmpVec2));
        this.tmpVec2.y = 0;
        this.tmpVec2.nor();
        if (this.tmpVec2.isZero()) this.tmpVec2.set(this.lastUsedDir);
    }
}
