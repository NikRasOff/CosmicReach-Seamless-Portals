package com.nikrasoff.seamlessportals.animations;

public interface ISPAnimation {
    void update(float deltaTime);

    boolean isFinished();

    float getExtraTime();
    void restart();
}
