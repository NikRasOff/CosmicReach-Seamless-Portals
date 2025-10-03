package com.nikrasoff.seamlessportals.animations;

import com.badlogic.gdx.math.Vector3;

public class Vector3Animation extends SPAnimation<Vector3>{
    public boolean slerpMode = false;
    // To resolve problems with slerping
    public Vector3 revolveAxis = Vector3.Y.cpy();

    public Vector3Animation(Vector3 start, Vector3 end, float time, Vector3 updatedVector){
        super(start, end, time, updatedVector);
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        this.updatedValue.set(this.startingValue);
        if (slerpMode){
            if (this.startingValue.isCollinearOpposite(this.endingValue)){
                double delta = Math.PI / this.animEndTime * deltaTime;
                this.startingValue.rotateRad(this.revolveAxis, (float) delta);
                this.updatedValue.set(this.startingValue);
                this.animGoingFor -= deltaTime;
                this.animEndTime -= deltaTime;
                return;
            }
            this.updatedValue.slerp(this.endingValue, progress);
        }
        else{
            this.updatedValue.lerp(this.endingValue, this.progress);
        }
    }
}
