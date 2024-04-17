package com.nikrasoff.seamlessportals.config;

import dev.crmodders.modmenu.api.ConfigScreenFactory;
import dev.crmodders.modmenu.api.ModMenuApi;

public class SeamlessPortalsModMenuConfig implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return SeamlessPortalsConfigMenu::new;
    }
}
