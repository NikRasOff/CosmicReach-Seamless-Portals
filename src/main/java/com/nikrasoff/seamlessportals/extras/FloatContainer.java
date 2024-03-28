package com.nikrasoff.seamlessportals.extras;

public class FloatContainer {
    private float value = 0;

    public FloatContainer(){}
    public FloatContainer(float value){
        this.value = value;
    }

    public FloatContainer set(float value){
        this.value = value;
        return this;
    }

    public FloatContainer set(FloatContainer value){
        return this.set(value.value);
    }

    public float getValue(){
        return value;
    }
}
