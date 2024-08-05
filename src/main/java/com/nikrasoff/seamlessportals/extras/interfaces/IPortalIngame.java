package com.nikrasoff.seamlessportals.extras.interfaces;

import finalforeach.cosmicreach.entities.PlayerController;

public interface IPortalIngame {
    PlayerController getPlayerController();
    float getTempFovForPortals();
}
