package com.nikrasoff.seamlessportals.animations;

import com.badlogic.gdx.math.Quaternion;

public class QuaternionAnimation extends SPAnimation<Quaternion> {
    public QuaternionAnimation(Quaternion start, Quaternion end, float time, Quaternion updated) {
        super(start, end, time, updated);
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        this.updatedValue.set(this.startingValue);
        this.updatedValue.slerp(this.endingValue, this.progress);
    }
}
