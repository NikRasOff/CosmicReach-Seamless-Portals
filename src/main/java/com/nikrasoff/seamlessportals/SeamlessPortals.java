package com.nikrasoff.seamlessportals;

import com.nikrasoff.seamlessportals.items.HandheldPortalGen;
import com.nikrasoff.seamlessportals.models.EntityItemModel;
import com.nikrasoff.seamlessportals.portals.PortalManager;
import dev.crmodders.cosmicquilt.api.entrypoint.ModInitializer;
import dev.crmodders.flux.FluxRegistries;
import dev.crmodders.flux.assets.FluxGameAssetLoader;
import dev.crmodders.flux.block.DataModBlock;
import dev.crmodders.flux.events.OnLoadAssetsEvent;
import dev.crmodders.flux.events.OnPreLoadAssetsEvent;
import dev.crmodders.flux.events.OnRegisterBlockEvent;
import dev.crmodders.flux.localization.ILanguageFile;
import dev.crmodders.flux.localization.LanguageManager;
import dev.crmodders.flux.localization.files.LanguageFileVersion1;
import dev.crmodders.flux.tags.ResourceLocation;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.rendering.items.ItemRenderer;
import org.greenrobot.eventbus.Subscribe;
import org.quiltmc.loader.api.ModContainer;

import java.util.logging.Logger;

public class SeamlessPortals implements ModInitializer {
    public static PortalManager portalManager = new PortalManager();
    public static final String MOD_ID = "seamlessportals";
    public static final Logger LOGGER = Logger.getLogger(MOD_ID);
    @Override
    public void onInitialize(ModContainer modContainer) {
        FluxRegistries.EVENT_BUS.register(this);
        LOGGER.info("Initialising Seamless Portals!");

        SeamlessPortalsBlockEvents.registerSeamlessPortalsBlockEvents();
    }

    static String[] blockIds = {
            "portal_generator",
            "portal_destabiliser",
            "ph_portal",
            "ph_destabiliser_pulse"
    };

    @Subscribe
    public void onEvent(OnRegisterBlockEvent event){
        for (String id: blockIds){
            event.registerBlock(() -> new DataModBlock(id, new ResourceLocation(MOD_ID, "blocks/" + id + ".json")));
        }
    }

    @Subscribe
    public void onEvent(OnPreLoadAssetsEvent event){
        ItemRenderer.registerItemModelCreator(HandheldPortalGen.class, (handheldPortalGen) -> new EntityItemModel("seamlessportals:handheld_portal_gen.json", "seamlessportals:handheld_portal_gen.animation.json",
                "animation.handheld_portal_generator.idle", "seamlessportals:handheld_portal_gen.png"));
        ILanguageFile langEN = FluxGameAssetLoader.LOADER.loadResourceSync(new ResourceLocation(MOD_ID, "languages/en-US.json"), LanguageFileVersion1.class);
        LanguageManager.registerLanguageFile(langEN);
        ILanguageFile langRU = FluxGameAssetLoader.LOADER.loadResourceSync(new ResourceLocation(MOD_ID, "languages/ru-ru.json"), LanguageFileVersion1.class);
        LanguageManager.registerLanguageFile(langRU);
    }

    @Subscribe
    public void onEvent(OnLoadAssetsEvent event){
        Item.registerItem(new HandheldPortalGen());
    }
}
