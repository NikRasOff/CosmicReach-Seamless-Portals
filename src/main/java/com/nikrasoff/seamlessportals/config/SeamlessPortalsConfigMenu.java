package com.nikrasoff.seamlessportals.config;

import dev.crmodders.flux.FluxConstants;
import dev.crmodders.flux.FluxSettings;
import dev.crmodders.flux.api.gui.ButtonElement;
import dev.crmodders.flux.api.gui.TextElement;
import dev.crmodders.flux.localization.TranslationKey;
import dev.crmodders.flux.localization.TranslationString;
import dev.crmodders.flux.menus.LayoutMenu;
import finalforeach.cosmicreach.gamestates.GameState;

public class SeamlessPortalsConfigMenu extends LayoutMenu {
    private static final TranslationKey debugOutlinesKey = new TranslationKey("seamlessportals:config_menu.debug_outlines");
    private final ButtonElement debugOutlines;

    private void updateDedugOutlinesText(){
        if (this.debugOutlines == null) return;
        TranslationString text = FluxSettings.SelectedLanguage.getTranslatedString(debugOutlinesKey);
        TranslationString on = FluxSettings.SelectedLanguage.getTranslatedString(FluxConstants.TextOn);
        TranslationString off = FluxSettings.SelectedLanguage.getTranslatedString(FluxConstants.TextOff);
        boolean value = SeamlessPortalsConfig.INSTANCE.debugOutlines.value();
        this.debugOutlines.text = text.format(value ? on.string() : off.string());
        this.debugOutlines.updateText();
    }

    public SeamlessPortalsConfigMenu(GameState previousState){
        super(previousState);

        TextElement title = new TextElement(new TranslationKey("seamlessportals:config_menu.title"));
        title.backgroundEnabled = false;
        this.addFluxElement(title);

        this.addBackButton();

        this.debugOutlines = new ButtonElement(() -> {
            SeamlessPortalsConfig.INSTANCE.debugOutlines.setValue(!SeamlessPortalsConfig.INSTANCE.debugOutlines.value());
            this.updateDedugOutlinesText();
        });
        this.updateDedugOutlinesText();
        this.addFluxElement(this.debugOutlines);
    }
}
