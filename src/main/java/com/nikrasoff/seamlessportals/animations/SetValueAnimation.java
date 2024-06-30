package com.nikrasoff.seamlessportals.animations;

public class SetValueAnimation<T> implements ISPAnimation{
    private T changedValue;
    private T newValue;
    private boolean isFinished = false;
    private float extraTime = 0;

    public SetValueAnimation(T changedValue, T newValue){
        this.changedValue = changedValue;
        this.newValue = newValue;
    }

    @Override
    public void update(float deltaTime) {
        changedValue = newValue;
        extraTime = deltaTime;
        isFinished = true;
    }

    @Override
    public boolean isFinished() {
        return this.isFinished;
    }

    @Override
    public float getExtraTime() {
        return this.extraTime;
    }

    @Override
    public void restart() {
        this.isFinished = false;
        this.extraTime = 0;
    }
}
