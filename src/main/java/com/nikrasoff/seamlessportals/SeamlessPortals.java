package com.nikrasoff.seamlessportals;

import com.nikrasoff.seamlessportals.portals.PortalManager;
import dev.crmodders.cosmicquilt.api.entrypoint.ModInitializer;
import dev.crmodders.flux.api.events.GameEvents;
import dev.crmodders.flux.api.resource.ResourceLocation;
import dev.crmodders.flux.localization.LanguageFile;
import dev.crmodders.flux.localization.TranslationApi;
import dev.crmodders.flux.registry.FluxRegistries;
import finalforeach.cosmicreach.gamestates.GameState;
import org.quiltmc.loader.api.ModContainer;

import java.util.logging.Logger;

public class SeamlessPortals implements ModInitializer {
    public static PortalManager portalManager = new PortalManager();
    public static final String MOD_ID = "seamlessportals";
    public static final Logger LOGGER = Logger.getLogger(MOD_ID);
    @Override
    public void onInitialize(ModContainer modContainer) {
        System.out.println("Initialising Seamless Portals!");
        GameEvents.ON_REGISTER_LANGUAGE.register(() -> {
            LanguageFile lang = LanguageFile.loadLanguageFile(new ResourceLocation("seamlessportals", "languages/en-US.json").load());
            TranslationApi.registerLanguageFile(lang);
        });

        SeamlessPortalsBlockEvents.registerSeamlessPortalsBlockEvents();
        SeamlessPortalsCustomBlocks.registerCustomBlocks();
    }
}
