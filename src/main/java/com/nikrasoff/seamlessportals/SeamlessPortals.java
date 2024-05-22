package com.nikrasoff.seamlessportals;

import com.nikrasoff.seamlessportals.portals.PortalManager;
import dev.crmodders.cosmicquilt.api.entrypoint.ModInitializer;
import dev.crmodders.flux.api.v5.events.GameEvents;
import dev.crmodders.flux.api.v5.resource.ResourceLocation;
import dev.crmodders.flux.localization.LanguageFile;
import dev.crmodders.flux.localization.TranslationApi;
import org.quiltmc.loader.api.ModContainer;

import java.util.logging.Logger;

public class SeamlessPortals implements ModInitializer {
    public static PortalManager portalManager = new PortalManager();
    public static final String MOD_ID = "seamlessportals";
    public static final Logger LOGGER = Logger.getLogger(MOD_ID);
    @Override
    public void onInitialize(ModContainer modContainer) {
        LOGGER.info("Initialising Seamless Portals!");
        String[] langIds = {
                "en-US",
                "ru-ru"
        };

        GameEvents.ON_REGISTER_LANGUAGE.register(() -> {
            for (String langID : langIds){
                LanguageFile lang = LanguageFile.loadLanguageFile(new ResourceLocation(MOD_ID, "languages/" + langID + ".json").load());
                TranslationApi.registerLanguageFile(lang);
            }
        });

        SeamlessPortalsBlockEvents.registerSeamlessPortalsBlockEvents();
        SeamlessPortalsCustomBlocks.registerCustomBlocks();
    }
}
