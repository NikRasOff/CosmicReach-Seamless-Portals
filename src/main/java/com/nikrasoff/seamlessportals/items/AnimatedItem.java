package com.nikrasoff.seamlessportals.items;

import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.util.Identifier;
import io.github.puzzle.cosmic.api.entity.IEntity;
import io.github.puzzle.cosmic.api.item.IItemStack;
import io.github.puzzle.cosmic.api.item.ITickingItem;
import io.github.puzzle.cosmic.api.util.DataPointUtil;
import io.github.puzzle.cosmic.api.world.IZone;
import io.github.puzzle.cosmic.impl.data.point.DataPointManifest;
import io.github.puzzle.cosmic.impl.data.point.single.IntegerDataPoint;
import io.github.puzzle.cosmic.item.AbstractCosmicItem;

import static com.nikrasoff.seamlessportals.SeamlessPortalsConstants.MOD_ID;

public abstract class AnimatedItem extends AbstractCosmicItem implements ITickingItem {
    int textureCount;
    int ticksPerFrame;

    public AnimatedItem(int textureCount, int ticksPerFrame, String texturePrefix, Identifier id){
        super(id);
        for (int i = 0; i < textureCount; ++i){
            this.addTexture(ItemModelType.ITEM_MODEL_3D, Identifier.of(MOD_ID, texturePrefix + (i + 1) + ".png"));
        }
        this.textureCount = textureCount;
        this.ticksPerFrame = ticksPerFrame * 2;
    }

    private void advanceTexture(IItemStack itemStack, int byTicks){
        DataPointManifest tag = (DataPointManifest) DataPointUtil.getManifestFromStack((ItemStack) itemStack);
        if (!tag.has("tickCount")) {
            tag.put("tickCount", new IntegerDataPoint(0));
        }

        Integer tickCount = tag.get("tickCount").cast(Integer.class).getValue();
        int currentEntry = getCurrentTexture(itemStack);
        tickCount += byTicks;
        if (tickCount >= this.ticksPerFrame){
            tickCount = 0;
            currentEntry = (currentEntry + 1) % this.textureCount;
        }
        setCurrentTexture(itemStack, currentEntry);
        tag.get("tickCount").cast(Integer.class).setValue(tickCount);
    }

    private void advanceTexture(IItemStack itemStack){
        this.advanceTexture(itemStack, 1);
    }

    @Override
    public void tickStack(float fixedUpdateTimeStep, IItemStack itemStack, boolean isBeingHeld) {
        this.advanceTexture(itemStack);
    }

    @Override
    public void tickEntity(IZone zone, double deltaTime, IEntity entity, IItemStack itemStack) {
        this.advanceTexture(itemStack, 2);
    }
}
