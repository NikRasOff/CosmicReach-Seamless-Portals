package com.nikrasoff.seamlessportals.animations;

import com.badlogic.gdx.math.Vector3;

public class Vector3Animation extends SPAnimation<Vector3>{
    public Vector3Animation(Vector3 start, Vector3 end, float time, Vector3 updatedVector){
        super(start, end, time, updatedVector);
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        this.updatedValue.set(this.startingValue);
        this.updatedValue.lerp(this.endingValue, this.progress);
    }
}
