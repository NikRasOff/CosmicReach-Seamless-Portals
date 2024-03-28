package com.nikrasoff.seamlessportals.animations;

public abstract class SPAnimation<T> implements ISPAnimation {
    T updatedValue;
    T startingValue;
    T endingValue;
    float animEndTime;
    float animGoingFor;
    float progress;
    boolean isFinished;

    public SPAnimation(T start, T end, float time, T updated){
        this.updatedValue = updated;
        this.startingValue = start;
        this.endingValue = end;
        this.animGoingFor = 0;
        this.animEndTime = time;
        this.isFinished = false;
    }

    public void update(float deltaTime){
        this.animGoingFor += deltaTime;
        if (this.animGoingFor > this.animEndTime){
            this.progress = 1;
            this.isFinished = true;
            return;
        }
        this.progress = this.animGoingFor / this.animEndTime;
    }

    public boolean isFinished(){
        return this.isFinished;
    }

    @Override
    public float getExtraTime() {
        return this.isFinished() ? this.animGoingFor - this.animEndTime : 0;
    }
}
