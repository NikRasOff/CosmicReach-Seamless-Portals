package com.nikrasoff.seamlessportals.config;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.ScreenUtils;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.ui.FontRenderer;
import finalforeach.cosmicreach.ui.HorizontalAnchor;
import finalforeach.cosmicreach.ui.UIElement;
import finalforeach.cosmicreach.ui.VerticalAnchor;

import static com.nikrasoff.seamlessportals.SeamlessPortals.MOD_ID;

public class SeamlessPortalsConfigMenu extends GameState {
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
                String text = "Debug outlines: " + (value ? "On" : "Off");
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
                this.setText("Back");
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
        String title = "Seamless Portals config menu";
        FontRenderer.drawText(batch, this.uiViewport, title, 0, 15, HorizontalAnchor.CENTERED, VerticalAnchor.TOP_ALIGNED);
        batch.end();

        if (!this.firstFrame && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){
            goBack();
        }
    }
}
