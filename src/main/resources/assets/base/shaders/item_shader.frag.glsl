#version 150
#ifdef GL_ES
precision mediump float;
#endif

// Contributors: Shfloop, Mr-Zombii

in vec2 v_texCoord0;
in vec3 v_normal;
in vec3 worldPos;

uniform sampler2D texDiffuse;
uniform sampler2D u_hueMask;
uniform vec4 u_colorMask;
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

vec3 rgb2hsv(vec3 c) {
    vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));

    float d = q.x - min(q.w, q.y);
    float e = 1.0e-10;
    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

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

    vec4 maskTexColor = texture(u_hueMask, v_texCoord0);
    if (maskTexColor.a > 0.0)
    {
        vec3 hsvTex = rgb2hsv(texColor.rgb);
        vec3 hsvMask = rgb2hsv(u_colorMask.rgb);

        vec3 finalHSV = vec3(hsvTex.x + hsvMask.x, hsvTex.y * hsvMask.y, hsvTex.z * hsvMask.z);
        //vec3 finalHSV = vec3(hsvMask.x, min(hsvTex.y, hsvMask.y), 1);
        vec3 finalRGB = hsv2rgb(finalHSV);
        //vec3 finalRGB = u_colorMask.rgb;
        finalRGB = mix(finalRGB, u_colorMask.rgb, 1 - hsvMask.y);

        texColor.rgb = mix(texColor.rgb, finalRGB, maskTexColor.a);
        //texColor.rgb = vec3(hsvMask.y);
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