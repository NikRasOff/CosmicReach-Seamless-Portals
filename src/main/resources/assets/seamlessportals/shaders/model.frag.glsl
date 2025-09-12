#version 150
#ifdef GL_ES
precision mediump float;
#endif

uniform float u_fogDensity;
uniform vec3 u_sunDirection;
uniform vec3 u_cameraPosition;
uniform vec3 u_skyAmbientColor;

uniform sampler2D u_diffuseTex;
uniform vec4 u_ambientLight;

uniform vec3 u_portalNormal;
uniform vec3 u_portalOrigin;
uniform bool u_turnOnSlicing = false;
uniform int u_invertPortalNormal;

in vec2 v_texCoord0;
in vec3 v_worldPos;

out vec4 outColor;

vec3 applyFog(vec3 fogBaseColor, vec3 inputColor, float fogDensity, vec3 worldPos, vec3 cameraPosition)
{
    if(fogDensity == 0)
    {
        return inputColor;
    }

    float fogDistance = length(worldPos - cameraPosition);
    vec3 worldDirection = normalize(worldPos - cameraPosition);


    // Higher areas are less affected by fog.
    float higherDot = dot(worldDirection, vec3(0, 1, 0));
    float higherDistanceFactor = (higherDot + 1) / 2;
    higherDistanceFactor = pow(higherDistanceFactor, 0.75) * 0.04 / fogDensity;
    fogDistance = (fogDistance * (1-clamp(higherDistanceFactor, 0, 1)));

    float fogSpread = 1;

    float fogFactor = 1 - exp(-pow(fogDistance * fogDensity, fogSpread));
    fogFactor = clamp(fogFactor, 0.0, 1.0);

    float noonFactor = abs(dot(u_sunDirection, vec3(0, 1, 0)));
    float fogDirectionFactor = fogFactor * (1+dot(u_sunDirection, worldDirection))/2.0;
    fogDirectionFactor = clamp(fogDirectionFactor + ((higherDot + 0.5) / 2), 0, 1);
    fogDirectionFactor = 1 - ((1 - noonFactor) * (1 - fogDirectionFactor));

    // Higher areas are less affected by fog.
    float higherFactor = max(dot(worldDirection, vec3(0, 1, 0)), 0);
    higherFactor = pow(higherFactor, 0.75);
    fogFactor = (fogFactor * (1-higherFactor*0.5));

    vec3 fogColor = fogBaseColor * fogDirectionFactor;
    return mix(inputColor.rgb, fogColor, fogFactor);
}

vec3 getFogColor(vec3 fogBaseColor, vec3 blocklight, float fogDensity, vec3 worldPos, vec3 cameraPosition)
{
    float worldDistance = length(worldPos - cameraPosition);

    float blocklightFactor = exp(-pow(worldDistance * fogDensity/2, 0.4));
    return mix(fogBaseColor, max(fogBaseColor, blocklight.rgb), blocklightFactor);
}

void main() {
    // Portal slicing
    if (u_turnOnSlicing){
        vec3 portalCheckVec = v_worldPos - u_portalOrigin;
        if (dot(portalCheckVec, ((u_invertPortalNormal == 1) ? -u_portalNormal : u_portalNormal)) < 0){
            discard;
        }
    }

    vec4 tex = texture(u_diffuseTex, v_texCoord0);

    if(tex.a == 0)
    {
        discard;
    }

    outColor = tex * u_ambientLight;

    vec3 fogColor = getFogColor(u_skyAmbientColor, u_ambientLight.rgb, u_fogDensity, v_worldPos, u_cameraPosition);
    outColor.rgb = applyFog(fogColor, outColor.rgb, u_fogDensity, v_worldPos, u_cameraPosition);
}