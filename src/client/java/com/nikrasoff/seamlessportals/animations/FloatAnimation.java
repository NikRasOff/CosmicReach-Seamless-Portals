package com.nikrasoff.seamlessportals.animations;

import com.badlogic.gdx.math.MathUtils;
import com.nikrasoff.seamlessportals.extras.FloatContainer;

public class FloatAnimation extends SPAnimation<FloatContainer>{
    public FloatAnimation(float start, float end, float time, FloatContainer updated) {
        super(new FloatContainer(start), new FloatContainer(end), time, updated);
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        this.updatedValue.set(MathUtils.lerp(this.startingValue.getValue(), this.endingValue.getValue(), this.progress));
    }
}
