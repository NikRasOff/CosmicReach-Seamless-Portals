package com.nikrasoff.seamlessportals.mixin;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.nikrasoff.seamlessportals.extras.interfaces.IModEntity;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.entities.components.NameTagEntityComponent;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.entities.player.PlayerEntity;
import finalforeach.cosmicreach.entities.player.skins.GameTexturePlayerSkin;
import finalforeach.cosmicreach.singletons.GameSingletons;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityAnimationMixin extends EntityAnimationMixin implements IModEntity {
    @Shadow transient GameTexturePlayerSkin playerSkin;
    @Shadow public transient NameTagEntityComponent nameTagComponent;
    @Shadow private transient Player player;
    @Shadow String currentMovementAnimation;

    @Shadow abstract boolean isLocalPlayer();
    @Shadow abstract protected void updateSkin();
    @Shadow abstract void switchMovementAnimation(String animName);
    @Shadow abstract public Player getPlayer();

    @Override
    public void cosmicReach_Seamless_Portals$renderNoAnim(Camera renderCamera){
        if (this.modelInstance != null) {
            boolean isSelf = this.isLocalPlayer();
            if (this.playerSkin == null || this.playerSkin.isUpdated()) {
                this.updateSkin();
            }

            this.nameTagComponent.setVisible(true);
            String name = this.player.getAccount().getDisplayName();
            this.nameTagComponent.setName(name);
            super.cosmicReach_Seamless_Portals$renderNoAnim(renderCamera);

            if (isSelf) {
                boolean isFirstPerson = GameSingletons.client().isFirstPerson();
                this.nameTagComponent.setVisible(!isFirstPerson);
            }
        }
    }

    @Override
    public void cosmicReach_Seamless_Portals$renderSliced(Camera playerCamera, Portal portal) {
        if (this.modelInstance != null) {
            boolean isSelf = this.isLocalPlayer();
            if (this.playerSkin == null || this.playerSkin.isUpdated()) {
                this.updateSkin();
            }

            this.nameTagComponent.setVisible(true);
            String name = this.player.getAccount().getDisplayName();
            this.nameTagComponent.setName(name);
            super.cosmicReach_Seamless_Portals$renderSliced(playerCamera, portal);

            if (isSelf) {
                boolean isFirstPerson = GameSingletons.client().isFirstPerson();
                this.nameTagComponent.setVisible(!isFirstPerson);
            }
        }
    }

    @Override
    public void cosmicReach_Seamless_Portals$renderDuplicate(Camera playerCamera, Portal portal) {
        if (this.modelInstance != null) {
            boolean isSelf = this.isLocalPlayer();
            if (this.playerSkin == null || this.playerSkin.isUpdated()) {
                this.updateSkin();
            }

            this.nameTagComponent.setVisible(true);
            String name = this.player.getAccount().getDisplayName();
            this.nameTagComponent.setName(name);
            super.cosmicReach_Seamless_Portals$renderDuplicate(playerCamera, portal);

            if (isSelf) {
                boolean isFirstPerson = GameSingletons.client().isFirstPerson();
                this.nameTagComponent.setVisible(!isFirstPerson);
            }
        }
    }

    @Override
    public void cosmicReach_Seamless_Portals$advanceAnimations() {
        if (this.modelInstance == null) return;

        if (GameSingletons.isClient) {
            float horizSpeed = this.velocity.dst(0.0F, this.velocity.y, 0.0F);
            if (horizSpeed == 0.0F) {
                horizSpeed = Vector2.dst(this.position.x, this.position.z, this.lastPosition.x, this.lastPosition.z);
                horizSpeed /= 0.05F;
            }

            if (this.currentMovementAnimation == null) {
                this.switchMovementAnimation("basic_idle_cycle");
            }

            String nextMovementAnim = this.currentMovementAnimation;
            nextMovementAnim = "basic_idle_cycle";
            if (horizSpeed > 0.5F) {
                if (this.player.getEntity().isNoClip()) {
                    if (this.player.isProne) {
                        nextMovementAnim = "crawling_idle_cycle";
                    } else {
                        nextMovementAnim = "flying_cycle";
                    }
                } else if (this.player.isProne) {
                    nextMovementAnim = "crawling_cycle";
                } else if (this.isSneaking()) {
                    nextMovementAnim = "sneak_cycle";
                } else if (this.getPlayer().isSprinting) {
                    nextMovementAnim = "running_cycle";
                } else {
                    nextMovementAnim = "walk_cycle";
                }
            } else if (this.player.isProne) {
                nextMovementAnim = "crawling_idle_cycle";
            } else if (this.player.getEntity().isNoClip()) {
                nextMovementAnim = "flying_idle_cycle";
            } else if (this.isSneaking() && !this.player.isProne) {
                nextMovementAnim = "sneak_cycle";
            }

            this.switchMovementAnimation(nextMovementAnim);
        }

        super.cosmicReach_Seamless_Portals$advanceAnimations();
    }
}
