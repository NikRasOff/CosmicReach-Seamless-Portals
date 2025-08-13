#version 150
#ifdef GL_ES
precision mediump float;
#endif

in vec2 v_texCoord0;
in vec3 v_normal;
in vec3 v_worldPos;

uniform sampler2D texDiffuse;
uniform vec4 tintColor;
uniform int isInSlot;

uniform vec3 u_portalNormal;
uniform vec3 u_portalOrigin;
uniform int u_turnOnSlicing;
uniform int u_invertPortalNormal;

out vec4 outColor;

void main()
{
    // I didn't see any other way portal slicing could be implemented
    // besides overriding the shaders

    // Portal slicing
    if (u_turnOnSlicing == 1){
        vec3 portalCheckVec = v_worldPos - u_portalOrigin;
        if (dot(portalCheckVec, ((u_invertPortalNormal == 1) ? -u_portalNormal : u_portalNormal)) < 0){
            discard;
        }
    }

    //bs numbers might want to mess around with
    float faceShade = abs(dot(vec3(0,0,1), v_normal) ) + 0.6;
    faceShade *= abs(dot(vec3(0,1,0), v_normal) + 0.8);
    faceShade *= 1.0;
    vec4 texColor = texture(texDiffuse, v_texCoord0);

    if(texColor.a == 0)
    {
        discard;
    }

    if (isInSlot == 1) {
        outColor = texColor;
        return;
    } else outColor = vec4(texColor.rgb * faceShade , texColor.a) * tintColor;
}