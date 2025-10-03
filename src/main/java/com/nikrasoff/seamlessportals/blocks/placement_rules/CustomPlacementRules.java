package com.nikrasoff.seamlessportals.blocks.placement_rules;

import finalforeach.cosmicreach.blocks.placementrules.PlacementRules;

import java.util.Arrays;
import java.util.HashMap;

public class CustomPlacementRules extends PlacementRules {
    static final String[] defaultRuleIds = {
            "default",
            "stairs",
            "directional_towards",
            "directional_away",
            "omnidirectional_towards",
            "omnidirectional_away",
            "wall_ground_towards",
            "wall_ground_away",
            "axis"
    };
    static HashMap<String, CustomPlacementRules> placementRuleMap = new HashMap<>();

    static public void registerPlacementRules(String id, CustomPlacementRules rule){
        if (Arrays.asList(defaultRuleIds).contains(id) || placementRuleMap.containsKey(id)){
            throw new RuntimeException("Cannot have two rules under the same name");
        }
        placementRuleMap.put(id, rule);
    }

    static public CustomPlacementRules getPlacementRules(String id){
        return placementRuleMap.get(id);
    }

    static public void registerAllCustomPlacementRules(){
        registerPlacementRules("portal_gen", new PortalGenPlacementRules());
    }
}
