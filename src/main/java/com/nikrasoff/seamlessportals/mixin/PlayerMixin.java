package com.nikrasoff.seamlessportals.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.nikrasoff.seamlessportals.extras.interfaces.IPortalableEntity;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.world.Zone;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Shadow private Entity entity;

    @WrapOperation(method = "proneCheck", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/world/Zone;getBlockState(III)Lfinalforeach/cosmicreach/blocks/BlockState;"))
    private BlockState proneCheckMixin(Zone instance, int x, int y, int z, Operation<BlockState> original){
        IPortalableEntity portalableEntity = (IPortalableEntity) this.entity;
        return portalableEntity.checkIfShouldCollidePortal(instance, x, y, z, original);
    }

    @WrapOperation(method = "crouchCheck", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/world/Zone;getBlockState(III)Lfinalforeach/cosmicreach/blocks/BlockState;"))
    private BlockState sneakCheckMixin(Zone instance, int x, int y, int z, Operation<BlockState> original){
        IPortalableEntity portalableEntity = (IPortalableEntity) this.entity;
        return portalableEntity.checkIfShouldCollidePortal(instance, x, y, z, original);
    }
}
