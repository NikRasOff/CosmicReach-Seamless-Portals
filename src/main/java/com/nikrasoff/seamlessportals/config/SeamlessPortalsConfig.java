package com.nikrasoff.seamlessportals.config;

import org.quiltmc.config.api.ReflectiveConfig;
import org.quiltmc.config.api.annotations.Comment;
import org.quiltmc.config.api.annotations.SerializedName;
import org.quiltmc.config.api.values.TrackedValue;
import org.quiltmc.loader.api.config.v2.QuiltConfig;

import static com.nikrasoff.seamlessportals.SeamlessPortals.MOD_ID;

public class SeamlessPortalsConfig extends ReflectiveConfig {
    public static final SeamlessPortalsConfig INSTANCE = QuiltConfig.create(MOD_ID, MOD_ID, SeamlessPortalsConfig.class);
    @Comment("When true, portal hitboxes will be visible")
    @SerializedName("debug_outlines")
    public final TrackedValue<Boolean> debugOutlines = this.value(false);
}
