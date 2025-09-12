package com.nikrasoff.seamlessportals.mixin;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.nikrasoff.seamlessportals.extras.interfaces.IModEntity;
import finalforeach.cosmicreach.TickRunner;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.EntityFallingBlock;
import finalforeach.cosmicreach.singletons.GameSingletons;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityFallingBlock.class)
public abstract class FallingBlockEntityAnimationMixin extends EntityAnimationMixin implements IModEntity {
    @Shadow
    BlockState blockState;
    @Shadow float renderSize;

    @Override
    public void cosmicReach_Seamless_Portals$renderNoAnim(Camera renderCamera) {
        if (renderCamera.frustum.boundsInFrustum(this.globalBoundingBox)) {
            if (this.modelInstance == null && this.blockState != null) {
                this.modelInstance = GameSingletons.itemEntityModelLoader.load(this.blockState);
            }

            Vector3 tempRenderPosSP = new Vector3();

            Matrix4 tempRenderMatrixSP = new Matrix4();

            if (this.modelInstance != null) {
                tempRenderMatrixSP.idt();
                tempRenderPosSP.set(this.lastRenderPosition);
                TickRunner.INSTANCE.partTickSlerp(tempRenderPosSP, this.position);
                tempRenderMatrixSP.translate(tempRenderPosSP);
                tempRenderMatrixSP.scl(this.renderSize);
                tempRenderMatrixSP.translate(-0.5F, -0.5F, -0.5F);
                this.cosmicReach_Seamless_Portals$renderAfterMatrixSetNoAnim(renderCamera, tempRenderMatrixSP,true);
            }

        }
    }
}
