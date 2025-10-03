package com.nikrasoff.seamlessportals.mixin;

import com.nikrasoff.seamlessportals.blocks.placement_rules.CustomPlacementRules;
import finalforeach.cosmicreach.blocks.placementrules.PlacementRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlacementRules.class)
public abstract class PlacementRulesMixin {
    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    private static void modifiedGet(String type, CallbackInfoReturnable<PlacementRules> cir){
        CustomPlacementRules rules = CustomPlacementRules.getPlacementRules(type);
        if (rules != null){
            cir.setReturnValue(rules);
        }
    }
}
