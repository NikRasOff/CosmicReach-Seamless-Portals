package com.nikrasoff.seamlessportals.extras;

public class FloatContainer {
    // Honestly, not sure if this class is even needed
    // But I'll keep it because why not
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

    @Override
    public String toString() {
        return "(" + this.value + ")";
    }
}
