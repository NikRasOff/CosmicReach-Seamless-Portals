package com.nikrasoff.seamlessportals.animations;

import com.badlogic.gdx.graphics.Color;

public class ColorAnimation extends SPAnimation<Color>{

    public ColorAnimation(Color start, Color end, float time, Color updatedColor) {
        super(start, end, time, updatedColor);
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        this.updatedValue.set(this.startingValue);
        this.updatedValue.lerp(this.endingValue, this.progress);
    }
}
