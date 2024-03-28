package com.nikrasoff.seamlessportals.animations;

import com.badlogic.gdx.utils.Array;

public class SPAnimationSequence implements ISPAnimation {
    Array<ISPAnimation> animationArray;
    int currentAnimation;
    boolean isFinished;
    boolean isParallel;
    float extraTime;

    public SPAnimationSequence(boolean parallel){
        this.animationArray = new Array<>();
        this.isParallel = parallel;
        this.currentAnimation = 0;
        this.isFinished = false;
        this.extraTime = 0;
    }
    public SPAnimationSequence(Array<ISPAnimation> animations, boolean parallel){
        this.animationArray = animations;
        this.currentAnimation = 0;
        this.isFinished = false;
        this.isParallel = parallel;
        this.extraTime = 0;
    }

    @Override
    public void update(float deltaTime) {
        if (this.isFinished) return;
        if (this.isParallel){
            this.extraTime = 0;
            boolean allDone = true;
            for (ISPAnimation animation : animationArray){
                animation.update(deltaTime);
                if (!animation.isFinished()) allDone = false;
                else {
                    if(this.extraTime > 0 && animation.getExtraTime() < this.extraTime){
                        this.extraTime = animation.getExtraTime();
                    }
                }
            }
            if (allDone) this.isFinished = true;
        }
        else{
            ISPAnimation curAnim = this.animationArray.get(this.currentAnimation);
            curAnim.update(deltaTime + this.extraTime);
            this.extraTime = 0;
            if (curAnim.isFinished()){
                this.extraTime = curAnim.getExtraTime();
                if (this.currentAnimation < this.animationArray.size - 1){
                    this.currentAnimation += 1;
                }
                else {
                    this.isFinished = true;
                }
            }
        }
    }

    public void add(ISPAnimation newAnimation){
        this.animationArray.add(newAnimation);
    }

    @Override
    public boolean isFinished() {
        return this.isFinished;
    }

    @Override
    public float getExtraTime() {
        return this.extraTime;
    }

    public int getCurrentAnimationID(){
        return this.currentAnimation;
    }

    public void finish(){
        this.isFinished = true;
    }
}
