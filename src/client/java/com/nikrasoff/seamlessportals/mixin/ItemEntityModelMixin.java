package com.nikrasoff.seamlessportals.mixin;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.nikrasoff.seamlessportals.extras.interfaces.IModEntityModelInstance;
import com.nikrasoff.seamlessportals.extras.interfaces.ISliceableItemModel;
import com.nikrasoff.seamlessportals.extras.interfaces.ISpecialItemModel;
import com.nikrasoff.seamlessportals.portals.Portal;
import dev.puzzleshq.puzzleloader.loader.util.ReflectionUtil;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.ItemEntity;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.rendering.entities.instances.ItemEntityModelInstance;
import finalforeach.cosmicreach.rendering.items.ItemModel;
import io.github.puzzle.cosmic.impl.client.item.CosmicItemModelWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemEntityModelInstance.class)
public abstract class ItemEntityModelMixin implements IModEntityModelInstance {
    @Shadow private ItemModel model;

    @Shadow public abstract void render(Entity entity, Camera worldCamera, Matrix4 modelMat, boolean shouldRender);

    @Override
    public void cosmicReach_Seamless_Portals$renderNoAnim(Entity entity, Camera worldCamera, Matrix4 modelMat, boolean shouldRender) {
        if (shouldRender && this.model instanceof ISpecialItemModel specialItemModel){
            specialItemModel.renderAsItemEntityThroughPortal(entity.position, worldCamera, modelMat);
        }
        else this.render(entity, worldCamera, modelMat, shouldRender);
    }

    @Override
    public void cosmicReach_Seamless_Portals$renderSliced(Entity entity, Camera renderCamera, Matrix4 modelMatrix, Portal portal, boolean isDuplicate) {
        if (this.model instanceof ISliceableItemModel m){
            m.renderAsSlicedEntity(entity.position, ((ItemEntity) entity).getItemStack(), renderCamera, modelMatrix, portal, isDuplicate);
        }
        else this.render(entity, renderCamera, modelMatrix, true);
    }
}
