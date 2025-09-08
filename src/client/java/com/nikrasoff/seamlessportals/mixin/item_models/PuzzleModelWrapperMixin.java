package com.nikrasoff.seamlessportals.mixin.item_models;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.nikrasoff.seamlessportals.extras.interfaces.ISliceableItemModel;
import com.nikrasoff.seamlessportals.extras.interfaces.ISliceablePuzzleModel;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.items.ItemStack;
import io.github.puzzle.cosmic.api.client.model.ICosmicItemModel;
import io.github.puzzle.cosmic.api.item.IItemStack;
import io.github.puzzle.cosmic.impl.client.item.CosmicItemModelWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CosmicItemModelWrapper.class)
public abstract class PuzzleModelWrapperMixin implements ISliceableItemModel {
    @Shadow
    ICosmicItemModel parent;

    @Shadow public abstract void renderAsItemEntity(Vector3 vector3, Camera camera, Matrix4 matrix4);

    @Shadow public abstract void renderAsEntity(Vector3 pos, ItemStack stack, Camera entityCam, Matrix4 tmpMatrix);

    @Override
    public void renderAsSlicedEntity(Vector3 position, Camera renderCamera, Matrix4 modelMatrix, Portal portal, boolean isDuplicate) {
        if (this.parent instanceof ISliceablePuzzleModel s){
            s.renderAsSlicedEntity(position, null, renderCamera, modelMatrix, portal, isDuplicate);
        }
        else {
            this.renderAsItemEntity(position, renderCamera, modelMatrix);
        }
    }

    @Override
    public void renderAsSlicedPuzzleEntity(Vector3 position, ItemStack stack, Camera renderCamera, Matrix4 modelMatrix, Portal portal, boolean isDuplicate) {
        if (this.parent instanceof ISliceablePuzzleModel s){
            s.renderAsSlicedEntity(position, stack, renderCamera, modelMatrix, portal, isDuplicate);
        }
        else {
            this.renderAsEntity(position, stack, renderCamera, modelMatrix);
        }
    }
}
