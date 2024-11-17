package com.nikrasoff.seamlessportals.animations;

public class WaitAnimation implements ISPAnimation{
    private final float endTime;
    private float passedTime = 0;

    public WaitAnimation(float time){
        this.endTime = time;
    }

    @Override
    public void update(float deltaTime) {
        passedTime += deltaTime;
    }

    @Override
    public boolean isFinished() {
        return (passedTime > endTime);
    }

    @Override
    public float getExtraTime() {
        return Math.max(0, passedTime - endTime);
    }

    @Override
    public void restart() {
        passedTime = 0;
    }
}
