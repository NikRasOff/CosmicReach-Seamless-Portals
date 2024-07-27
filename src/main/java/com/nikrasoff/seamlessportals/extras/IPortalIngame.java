package com.nikrasoff.seamlessportals.extras;

import finalforeach.cosmicreach.entities.PlayerController;

public interface IPortalIngame {
    PlayerController getPlayerController();
    float getTempFovForPortals();
}
