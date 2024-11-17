package com.nikrasoff.seamlessportals.animations;

import com.badlogic.gdx.math.Matrix4;

public class Matrix4Animation extends SPAnimation<Matrix4>{
    public Matrix4Animation(Matrix4 start, Matrix4 end, float time, Matrix4 updated) {
        super(start, end, time, updated);
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        this.updatedValue.set(this.startingValue);
        this.updatedValue.lerp(this.endingValue, this.progress);
    }
}
