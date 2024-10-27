package com.nikrasoff.seamlessportals.ui.widgets;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.utils.Array;

public class TextureSwitchWidget extends Widget {
    public Array<Texture> textures;
    private int currentTexture;
    public TextureSwitchWidget(int startingTexture, Texture... textures) {
        this.currentTexture = startingTexture;
        this.textures = new Array<>(textures);
        this.setSize(this.getPrefWidth(), this.getPrefHeight());
    }

    public TextureSwitchWidget(Texture... textures){
        this(0, textures);
    }

    public void setCurrentTexture(int t){
        this.currentTexture = t;
    }

    public float getPrefWidth() {
        return 32.0F;
    }

    public float getPrefHeight() {
        return 32.0F;
    }

    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        float x = this.getX();
        float y = this.getY();
        Texture drawnTexture = this.textures.get(this.currentTexture);
        if (drawnTexture == null) return;

        batch.draw(drawnTexture, x, y);
    }
}
