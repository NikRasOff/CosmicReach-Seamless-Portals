package com.nikrasoff.seamlessportals.mixin;

import finalforeach.cosmicreach.rendering.entities.EntityModelInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityModelInstance.class)
public interface EntityModelInstanceMixin {
    @Accessor(value = "animTimer")
    float getAnimTimer();

    @Accessor(value = "animTimer")
    void setAnimTimer(float value);
}
