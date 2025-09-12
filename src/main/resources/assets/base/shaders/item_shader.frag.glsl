#version 150
#ifdef GL_ES
precision mediump float;
#endif

// Contributors: Shfloop, Mr-Zombii

in vec2 v_texCoord0;
in vec3 v_normal;
in vec3 worldPos;

uniform sampler2D texDiffuse;
uniform vec4 tintColor;
uniform int isInSlot;


uniform float u_fogDensity;
uniform vec3 u_sunDirection;
uniform vec3 cameraPosition;
uniform vec3 skyAmbientColor;

uniform vec3 u_portalNormal;
uniform vec3 u_portalOrigin;
uniform bool u_turnOnSlicing = false;
uniform int u_invertPortalNormal;

out vec4 outColor;

#import "base:shaders/common/fog.glsl"

void main()
{
    if (u_turnOnSlicing){
        vec3 portalCheckVec = worldPos - u_portalOrigin;
        if (dot(portalCheckVec, ((u_invertPortalNormal == 1) ? -u_portalNormal : u_portalNormal)) < 0){
            discard;
        }
    }

    // arbitrary numbers might want to mess around with
    float faceShade = abs(dot(vec3(0,0,1), v_normal) ) + 0.6;
    faceShade *= abs(dot(vec3(0,1,0), v_normal) + 0.8);
    faceShade *= 1.0;
    vec4 texColor = texture(texDiffuse, v_texCoord0);

    if(texColor.a == 0)
    {
        discard;
    }

    if (isInSlot == 1) 
    {
        outColor = texColor;
        return;
    } else
    {
        outColor = vec4(texColor.rgb * faceShade , texColor.a) * tintColor;

        vec3 fogColor = getFogColor(skyAmbientColor, tintColor.rgb, u_fogDensity, worldPos, cameraPosition);
        outColor.rgb = applyFog(fogColor, outColor.rgb, u_fogDensity, worldPos, cameraPosition);
    }
}