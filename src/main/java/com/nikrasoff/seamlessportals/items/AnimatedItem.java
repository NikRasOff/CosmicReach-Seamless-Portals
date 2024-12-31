package com.nikrasoff.seamlessportals.items;

import com.github.puzzle.game.items.IModItem;
import com.github.puzzle.game.items.ITickingItem;
import com.github.puzzle.game.items.data.DataTag;
import com.github.puzzle.game.items.data.DataTagManifest;
import com.github.puzzle.game.items.data.attributes.IntDataAttribute;
import com.github.puzzle.game.util.DataTagUtil;
import finalforeach.cosmicreach.entities.ItemEntity;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.util.Identifier;
import finalforeach.cosmicreach.world.Zone;

import static com.nikrasoff.seamlessportals.SeamlessPortalsConstants.MOD_ID;

public abstract class AnimatedItem implements IModItem, ITickingItem {
    public final DataTagManifest tagManifest = new DataTagManifest();

    int textureCount;
    int ticksPerFrame;

    public AnimatedItem(int textureCount, int ticksPerFrame, String texturePrefix){
        for (int i = 0; i < textureCount; ++i){
            this.addTexture(IModItem.MODEL_2_5D_ITEM, Identifier.of(MOD_ID, texturePrefix + (i + 1) + ".png"));
        }
        this.textureCount = textureCount;
        this.ticksPerFrame = ticksPerFrame;
    }

    @Override
    public DataTagManifest getTagManifest() {
        return tagManifest;
    }

    @Override
    public abstract Identifier getIdentifier();
    @Override
    public abstract String getName();

    @Override
    public abstract int getMaxStackSize();

    @Override
    public abstract boolean isCatalogHidden();

    @Override
    public void tickStack(float fixedUpdateTimeStep, ItemStack itemStack, boolean isBeingHeld) {
        DataTagManifest tag = DataTagUtil.getManifestFromStack(itemStack);
        if (!tag.hasTag("currentEntry")) {
            tag.addTag(new DataTag<>("currentEntry", new IntDataAttribute(0)));
        }
        if (!tag.hasTag("tickCount")) {
            tag.addTag(new DataTag<>("tickCount", new IntDataAttribute(0)));
        }

        Integer tickCount = tag.getTag("tickCount").getTagAsType(Integer.class).getValue();
        Integer currentEntry = tag.getTag("currentEntry").getTagAsType(Integer.class).getValue();
        tickCount += 1;
        if (tickCount >= this.ticksPerFrame * 2){
            tickCount = 0;
            currentEntry = (currentEntry + 1) % this.textureCount;
        }
        tag.addTag(new DataTag<>("currentEntry", new IntDataAttribute(currentEntry)));
        tag.addTag(new DataTag<>("tickCount", new IntDataAttribute(tickCount)));
        DataTagUtil.setManifestOnStack(tag, itemStack);
    }

    @Override
    public void tickEntity(Zone zone, double deltaTime, ItemEntity itemEntity, ItemStack itemStack) {
        DataTagManifest tag = DataTagUtil.getManifestFromStack(itemStack);
        if (!tag.hasTag("currentEntry")) {
            tag.addTag(new DataTag<>("currentEntry", new IntDataAttribute(0)));
        }
        if (!tag.hasTag("tickCount")) {
            tag.addTag(new DataTag<>("tickCount", new IntDataAttribute(0)));
        }

        Integer tickCount = tag.getTag("tickCount").getTagAsType(Integer.class).getValue();
        Integer currentEntry = tag.getTag("currentEntry").getTagAsType(Integer.class).getValue();
        tickCount += 1;
        if (tickCount >= this.ticksPerFrame){
            tickCount = 0;
            currentEntry = (currentEntry + 1) % this.textureCount;
        }
        tag.addTag(new DataTag<>("currentEntry", new IntDataAttribute(currentEntry)));
        tag.addTag(new DataTag<>("tickCount", new IntDataAttribute(tickCount)));
        DataTagUtil.setManifestOnStack(tag, itemStack);
    }
}
