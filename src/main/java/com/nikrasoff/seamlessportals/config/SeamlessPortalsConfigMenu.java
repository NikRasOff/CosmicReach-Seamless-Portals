package com.nikrasoff.seamlessportals.config;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.ScreenUtils;
import dev.crmodders.flux.FluxConstants;
import dev.crmodders.flux.FluxSettings;
import dev.crmodders.flux.localization.LanguageManager;
import dev.crmodders.flux.localization.TranslationKey;
import dev.crmodders.flux.localization.TranslationString;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.io.ChunkSaver;
import finalforeach.cosmicreach.lang.Lang;
import finalforeach.cosmicreach.ui.FontRenderer;
import finalforeach.cosmicreach.ui.HorizontalAnchor;
import finalforeach.cosmicreach.ui.UIElement;
import finalforeach.cosmicreach.ui.VerticalAnchor;

import static com.nikrasoff.seamlessportals.SeamlessPortals.MOD_ID;

public class SeamlessPortalsConfigMenu extends GameState {
    private static final TranslationKey debugOutlinesKey = new TranslationKey(MOD_ID + ":config_menu.debug_outlines");

    public GameState previousState;

    public SeamlessPortalsConfigMenu(GameState prevState){
        super.create();
        this.previousState = prevState;
//        title.backgroundEnabled = false;
//        this.addFluxElement(title);
//
//        this.addBackButton();
        UIElement debugOutlines = new UIElement(15, 75, 250, 50){
            @Override
            public void onCreate() {
                super.onCreate();
                this.vAnchor = VerticalAnchor.TOP_ALIGNED;
                this.hAnchor = HorizontalAnchor.LEFT_ALIGNED;
                this.updateText();
            }

            @Override
            public void onClick() {
                super.onClick();
                SeamlessPortalsConfig.INSTANCE.debugOutlines.setValue(!SeamlessPortalsConfig.INSTANCE.debugOutlines.value());
                this.updateText();
            }

            @Override
            public void updateText() {
                super.updateText();
                boolean value = SeamlessPortalsConfig.INSTANCE.debugOutlines.value();
                String on = LanguageManager.string(FluxConstants.TextOn);
                String off = LanguageManager.string(FluxConstants.TextOff);
                String text = LanguageManager.format(debugOutlinesKey, value ? on : off);
                setText(text);
            }
        };
        debugOutlines.show();
        this.uiObjects.add(debugOutlines);
        UIElement backButton = new UIElement(0, -15, 250, 50){
            @Override
            public void onCreate() {
                super.onCreate();
                this.hAnchor = HorizontalAnchor.CENTERED;
                this.vAnchor = VerticalAnchor.BOTTOM_ALIGNED;
                this.setText(LanguageManager.string(FluxConstants.TextBack));
            }

            @Override
            public void onClick() {
                super.onClick();
                goBack();
            }
        };
        backButton.show();
        this.uiObjects.add(backButton);
    }

    public void goBack(){
        GameState.switchToGameState(this.previousState);
    }

    public void render() {
        super.render();

        ScreenUtils.clear(0, 0, 0, 1.0F, true);
        Gdx.gl.glEnable(2929);
        Gdx.gl.glDepthFunc(513);
        Gdx.gl.glEnable(2884);
        Gdx.gl.glCullFace(1029);
        Gdx.gl.glEnable(3042);
        Gdx.gl.glBlendFunc(770, 771);

        this.drawUIElements();

        batch.setProjectionMatrix(this.uiCamera.combined);
        batch.begin();
        String title = LanguageManager.string(new TranslationKey("seamlessportals:config_menu.title"));
        FontRenderer.drawText(batch, this.uiViewport, title, 0, 15, HorizontalAnchor.CENTERED, VerticalAnchor.TOP_ALIGNED);
        batch.end();

        if (!this.firstFrame && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){
            goBack();
        }
    }
}
