package com.nikrasoff.seamlessportals.ui.widgets;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;

public class DoubleProgressTexture extends Widget {
    public Texture empty;
    public Texture full;
    public float progress;
    public Interpolation interpolation;
    Orientation direction;

    public DoubleProgressTexture(Texture empty, Texture full, Orientation direction) {
        this.interpolation = Interpolation.pow2;
        this.empty = empty;
        this.full = full;
        this.direction = direction;
        this.setSize(this.getPrefWidth(), this.getPrefHeight());
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
        float width = this.getWidth();
        float height = this.getHeight();
        float progress = this.interpolation.apply(this.progress) / 2;
        Texture back = this.empty;
        Texture front = this.full;

        float pw = width * progress;
        float ph = height * progress;
        float emptyU = 0.0F;
        float emptyV = 1.0F;
        float emptyW = 1.0F;
        float emptyH = 0.0F;
        float fullU = 0.0F;
        float fullV = 1.0F;
        float fullW = 1.0F;
        float fullH = 0.0F;
        switch (this.direction) {
            case HORIZONTAL:
                batch.draw(back, x + pw, y, width - pw * 2, height, progress, emptyV, emptyW - progress, emptyH);
                batch.draw(front, x, y, pw, height, fullU, fullV, progress, fullH);
                batch.draw(front, x + width - pw, y, pw, height, fullW - progress, fullV, fullW, fullH);
                break;
            case VERTICAL:
                fullH = 0.0F;
                batch.draw(back, x, y + pw, width, height - ph * 2, emptyU, progress, emptyW, emptyH - progress);
                batch.draw(front, x, y, width, ph, fullU, fullV, fullW, progress);
                batch.draw(front, x, y + height - pw, width, ph, fullU, fullH - progress, fullW, fullH);
        }

    }

    public void setProgress(float progressRatio) {
        this.progress = MathUtils.clamp(progressRatio, 0.0F, 1.0F);
    }

    public enum Orientation{
        HORIZONTAL,
        VERTICAL
    }
}
