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
        this.ticksPerFrame = ticksPerFrame;
    }

    @Override
    public void tickStack(float fixedUpdateTimeStep, IItemStack itemStack, boolean isBeingHeld) {
        DataPointManifest tag = (DataPointManifest) DataPointUtil.getManifestFromStack((ItemStack) itemStack);
        if (!tag.has("currentEntry")) {
            tag.put("currentEntry", new IntegerDataPoint(0));
        }
        if (!tag.has("tickCount")) {
            tag.put("tickCount", new IntegerDataPoint(0));
        }

        Integer tickCount = tag.get("tickCount").cast(Integer.class).getValue();
        Integer currentEntry = tag.get("currentEntry").cast(Integer.class).getValue();
        tickCount += 1;
        if (tickCount >= this.ticksPerFrame * 2){
            tickCount = 0;
            currentEntry = (currentEntry + 1) % this.textureCount;
        }
        tag.get("currentEntry").cast(Integer.class).setValue(currentEntry);
        tag.get("tickCount").cast(Integer.class).setValue(tickCount);
    }

    @Override
    public void tickEntity(IZone zone, double deltaTime, IEntity entity, IItemStack itemStack) {
        DataPointManifest tag = (DataPointManifest) DataPointUtil.getManifestFromStack((ItemStack) itemStack);
        if (!tag.has("currentEntry")) {
            tag.put("currentEntry", new IntegerDataPoint(0));
        }
        if (!tag.has("tickCount")) {
            tag.put("tickCount", new IntegerDataPoint(0));
        }

        Integer tickCount = tag.get("tickCount").cast(Integer.class).getValue();
        Integer currentEntry = tag.get("currentEntry").cast(Integer.class).getValue();
        tickCount += 1;
        if (tickCount >= this.ticksPerFrame){
            tickCount = 0;
            currentEntry = (currentEntry + 1) % this.textureCount;
        }
        tag.get("currentEntry").cast(Integer.class).setValue(currentEntry);
        tag.get("tickCount").cast(Integer.class).setValue(tickCount);
    }
}
