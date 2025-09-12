#version 150
#ifdef GL_ES 
precision mediump float;
#endif

uniform vec3 cameraPosition;
uniform vec3 skyAmbientColor;
uniform vec4 tintColor;
uniform vec3 worldAmbientColor;

#import "base:shaders/common/renderDistance.glsl"

in vec2 v_texCoord0;
in vec3 worldPos;
in vec4 blocklight;
in vec3 faceNormal;

uniform sampler2D texDiffuse;
uniform vec3 u_sunDirection;

uniform vec3 u_portalNormal;
uniform vec3 u_portalOrigin;
uniform bool u_turnOnSlicing = false;
uniform int u_invertPortalNormal;

out vec4 outColor;
uniform float u_fogDensity;

#import "base:shaders/common/fog.glsl"

void main()
{
    // Portal slicing
    if (u_turnOnSlicing){
        vec3 portalCheckVec = worldPos - u_portalOrigin;
        if (dot(portalCheckVec, ((u_invertPortalNormal == 1) ? -u_portalNormal : u_portalNormal)) < 0){
            discard;
        }
    }
    vec2 tilingTexCoords = v_texCoord0;

    vec4 texColor = texture(texDiffuse, v_texCoord0);

    float fadeOutDistance = (u_renderDistanceInChunks - 1) * 16;
    float fadeOutFactor = clamp((fadeOutDistance - length(worldPos.xz - cameraPosition.xz))/16.0, 0, 1);
    texColor.a = texColor.a * pow(fadeOutFactor, 0.5);

    if(texColor.a == 0)
    {
        discard;
    }

    float noonDot = dot(u_sunDirection, faceNormal);
    //noonDot = 1;
    noonDot = sign(noonDot) * sqrt(abs(noonDot));
    vec3 blockAmbientColor = skyAmbientColor * max(noonDot, 0.5);

    // https://www.desmos.com/calculator
    // y\ =\ \frac{30}{1+e^{-15\left(\frac{x}{25}\right)^{2}}}-15
    vec3 it =  pow(15*blocklight.rgb / 25.0, vec3(2));
    vec3 t = 30.0/(1.0 + exp(-15.0 * it)) - 15;
    vec3 lightTint = max(t/15, blocklight.a * blockAmbientColor);

    //lightTint = max(lightTint, vec3(0.1));
    //texColor = vec4(1);

    outColor = tintColor * vec4(texColor.rgb * lightTint, texColor.a);

    vec3 fogColor = skyAmbientColor;//vec3(1) - pow(vec3(1) - skyAmbientColor, vec3(2));
    fogColor = getFogColor(fogColor, blocklight.rgb, u_fogDensity, worldPos, cameraPosition);
    outColor.rgb = applyFog(fogColor, outColor.rgb, u_fogDensity, worldPos, cameraPosition);

    outColor.rgb = max(outColor.rgb, texColor.rgb * worldAmbientColor);


    float gamma = 1.1;//1.5;
    outColor.rgb = pow(outColor.rgb, vec3(1.0/gamma));
}