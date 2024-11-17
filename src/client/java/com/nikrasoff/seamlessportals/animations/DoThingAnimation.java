package com.nikrasoff.seamlessportals.animations;

public class DoThingAnimation<T> implements ISPAnimation{
    private Runnable thingToDo;
    private boolean isFinished = false;
    private float extraTime = 0;

    public DoThingAnimation(Runnable thingToDo){
        this.thingToDo = thingToDo;
    }

    @Override
    public void update(float deltaTime) {
        thingToDo.run();
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
