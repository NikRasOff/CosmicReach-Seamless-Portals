package com.nikrasoff.seamlessportals.mixin;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.nikrasoff.seamlessportals.extras.interfaces.IModEntityModelInstance;
import com.nikrasoff.seamlessportals.extras.interfaces.IPortalRenderEntityComponent;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.entities.CommonEntityTags;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.components.JetpackComponent;
import finalforeach.cosmicreach.rendering.entities.IEntityAnimation;
import finalforeach.cosmicreach.rendering.entities.IEntityModel;
import finalforeach.cosmicreach.rendering.entities.IEntityModelInstance;
import finalforeach.cosmicreach.rendering.entities.IEntityModelInstancePlayer;
import finalforeach.cosmicreach.singletons.GameSingletons;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(JetpackComponent.class)
public abstract class JetpackComponentMixin implements IPortalRenderEntityComponent {
    @Shadow private IEntityModelInstancePlayer modelInstance;
    @Shadow
    public static IEntityModel jetpackModel;

    @Override
    public void renderNoAnim(Entity entity, Camera worldCamera, Vector3 tmpRenderPos, Matrix4 tmpModelMatrix, boolean shouldRender) {
        if (entity.hasTag(CommonEntityTags.USING_JETPACK)) {
            if (this.modelInstance == null) {
                if (jetpackModel == null) {
                    jetpackModel = GameSingletons.entityModelLoader.load("models/entities/jetpack.json", "animations/entities/player.animation.json", "basic_idle_cycle");
                }

                this.modelInstance = (IEntityModelInstancePlayer)jetpackModel.getNewModelInstance(IEntityModelInstancePlayer.class);
            }

            float r = entity.modelLightColor.r;
            float g = entity.modelLightColor.g;
            float b = entity.modelLightColor.b;
            if (entity.recentlyHit()) {
                b = 0.0F;
                g = 0.0F;
            }

            IEntityModelInstance m = entity.modelInstance;
            if (m != null) {
                Array<? extends IEntityAnimation> a = m.getAnimations();
                this.modelInstance.shadowAnimations(a);
            }

            this.modelInstance.setTint(r, g, b, 1.0F);
            ((IModEntityModelInstance) this.modelInstance).cosmicReach_Seamless_Portals$renderNoAnim(entity, worldCamera, tmpModelMatrix, shouldRender);
        }
    }

    @Override
    public void renderSliced(Entity entity, Camera worldCamera, Portal portal, Vector3 tmpRenderPos, Matrix4 tmpModelMatrix, boolean shouldRender) {
        if (entity.hasTag(CommonEntityTags.USING_JETPACK)) {
            if (this.modelInstance == null) {
                if (jetpackModel == null) {
                    jetpackModel = GameSingletons.entityModelLoader.load("models/entities/jetpack.json", "animations/entities/player.animation.json", "basic_idle_cycle");
                }

                this.modelInstance = (IEntityModelInstancePlayer)jetpackModel.getNewModelInstance(IEntityModelInstancePlayer.class);
            }

            float r = entity.modelLightColor.r;
            float g = entity.modelLightColor.g;
            float b = entity.modelLightColor.b;
            if (entity.recentlyHit()) {
                b = 0.0F;
                g = 0.0F;
            }

            IEntityModelInstance m = entity.modelInstance;
            if (m != null) {
                Array<? extends IEntityAnimation> a = m.getAnimations();
                this.modelInstance.shadowAnimations(a);
            }

            this.modelInstance.setTint(r, g, b, 1.0F);
            ((IModEntityModelInstance) this.modelInstance).cosmicReach_Seamless_Portals$renderSliced(entity, worldCamera, tmpModelMatrix, portal, false);
        }
    }

    @Override
    public void renderDuplicate(Entity entity, Camera worldCamera, Portal portal, Vector3 tmpRenderPos, Matrix4 tmpModelMatrix, boolean shouldRender) {
        if (entity.hasTag(CommonEntityTags.USING_JETPACK)) {
            if (this.modelInstance == null) {
                if (jetpackModel == null) {
                    jetpackModel = GameSingletons.entityModelLoader.load("models/entities/jetpack.json", "animations/entities/player.animation.json", "basic_idle_cycle");
                }

                this.modelInstance = (IEntityModelInstancePlayer)jetpackModel.getNewModelInstance(IEntityModelInstancePlayer.class);
            }

            float r = entity.modelLightColor.r;
            float g = entity.modelLightColor.g;
            float b = entity.modelLightColor.b;
            if (entity.recentlyHit()) {
                b = 0.0F;
                g = 0.0F;
            }

            IEntityModelInstance m = entity.modelInstance;
            if (m != null) {
                Array<? extends IEntityAnimation> a = m.getAnimations();
                this.modelInstance.shadowAnimations(a);
            }

            this.modelInstance.setTint(r, g, b, 1.0F);
            ((IModEntityModelInstance) this.modelInstance).cosmicReach_Seamless_Portals$renderSliced(entity, worldCamera, tmpModelMatrix, portal, true);
        }
    }
}
