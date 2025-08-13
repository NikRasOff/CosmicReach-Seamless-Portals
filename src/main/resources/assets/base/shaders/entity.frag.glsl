#version 150
#ifdef GL_ES 
precision mediump float;
#endif

in vec2 v_texCoord0;
in vec3 worldPos;


uniform float u_fogDensity;
uniform vec3 u_sunDirection;
uniform vec3 cameraPosition;
uniform vec3 skyAmbientColor;
uniform vec3 trueCameraPosition;
uniform bool u_applyFog;

uniform sampler2D texDiffuse;
uniform sampler2D texEmission;
uniform vec4 tintColor;

uniform vec3 u_portalNormal;
uniform vec3 u_portalOrigin;
uniform int u_turnOnSlicing;
uniform int u_invertPortalNormal;

out vec4 outColor;

#import "base:shaders/common/fog.glsl"

void main() 
{
    // Portal slicing
    if (u_turnOnSlicing == 1){
        vec3 portalCheckVec = worldPos - u_portalOrigin;
        if (dot(portalCheckVec, ((u_invertPortalNormal == 1) ? -u_portalNormal : u_portalNormal)) < 0){
            discard;
        }
    }
    vec4 texColor = texture(texDiffuse, v_texCoord0);

    if(texColor.a == 0)
    {
        discard;
    }

    outColor = texColor * tintColor;
    vec4 emissionColor = texture(texEmission, v_texCoord0);
    outColor.rgb = max(outColor.rgb, emissionColor.rgb * emissionColor.a);

    vec3 fogColor = getFogColor(skyAmbientColor, tintColor.rgb, u_fogDensity, worldPos, trueCameraPosition);

    if(u_applyFog)
    {
        outColor.rgb = applyFog(fogColor, outColor.rgb, u_fogDensity, worldPos, trueCameraPosition);
    }
}