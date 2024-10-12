package com.nikrasoff.seamlessportals.extras.interfaces;

import finalforeach.cosmicreach.io.CRBinDeserializer;
import finalforeach.cosmicreach.io.CRBinSerializer;

import java.util.Map;

public interface CustomPropertyItem {
    void readCustomProperties(CRBinDeserializer crbd, Map<String, Object> customProperties);
    void writeCustomProperties(CRBinSerializer crbs, Map<String, Object> customProperties);
}
