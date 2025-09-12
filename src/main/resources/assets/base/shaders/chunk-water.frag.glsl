#version 150
#ifdef GL_ES 
precision mediump float;
#endif

uniform float u_time;
uniform vec3 cameraPosition;
uniform vec3 skyAmbientColor;
uniform vec3 skyColor;
uniform vec4 tintColor;
uniform vec3 worldAmbientColor;
uniform vec3 u_sunDirection;
uniform bool u_isItem;

#import "base:shaders/common/renderDistance.glsl"

in vec2 v_texCoord0;
in vec4 blocklight;
in float waveStrength;
in vec3 worldPos;
in vec3 toCameraVector;

uniform sampler2D texDiffuse;
uniform sampler2D noiseTex;

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

    vec2 numTiles = floor(v_texCoord0);
    vec2 tilingTexCoords = v_texCoord0;
    
    if(numTiles.xy != vec2(0, 0))
    {
        tilingTexCoords = (v_texCoord0 - numTiles);
        vec2 flooredTexCoords = floor((v_texCoord0 - numTiles) * 16) / 16;
        numTiles = numTiles + vec2(1,1);

        tilingTexCoords = flooredTexCoords + mod(((tilingTexCoords - flooredTexCoords) * numTiles) * 16, 1) / 16;
    }
    float inSlotFactor = u_isItem ? 1 : 0;

    vec4 texColor = texture(texDiffuse, tilingTexCoords);

    vec3 viewVector = u_isItem ? vec3(0,0,1) : normalize(toCameraVector);
    vec3 faceNormal = vec3(0.0, 1.0, 0.0);
    float fresnel = abs(dot(viewVector, faceNormal));

    vec2 noiseUV = 0.2*vec2(waveStrength - 0.1) + worldPos.xz / 16.0;
    noiseUV += vec2(u_time*0.02);
    vec2 distortion = fresnel * texture(noiseTex, noiseUV).rg;
    vec3 waterColor = texColor.rgb;

    fresnel = pow(fresnel, mix(3, 1, 2*(waveStrength - 0.1 + distortion.r/3.0)));
    fresnel = pow(fresnel, 0.35);
    waterColor = mix(waterColor * 0.5, waterColor, 0.5 + 0.5*waveStrength*(1-fresnel));
    
    waterColor = mix(waterColor * 0.75, waterColor, fresnel);
    waterColor = mix(waterColor, skyColor, blocklight.a * (1-fresnel));


    vec3 lightTint = max(blocklight.rgb, blocklight.a * skyAmbientColor);
    float alpha = mix(texColor.a*2.0, texColor.a*0.5, fresnel);
    
    float fadeOutDistance = (u_renderDistanceInChunks - 1) * 16;
    float fadeOutFactor = clamp((fadeOutDistance - length(worldPos.xz - cameraPosition.xz))/16.0, 0, 1);
    alpha = alpha * pow(fadeOutFactor, 0.5);
    if(alpha == 0)
    {
        discard;
    }

    float blocklightOrSlotFactor = max(blocklight.a, inSlotFactor);

    outColor = vec4(mix(vec3(1), waterColor, blocklight.a) * lightTint, alpha);
    outColor.rgb = mix(outColor.rgb, skyColor, blocklightOrSlotFactor * (1-fresnel));
    outColor *= tintColor;
    outColor.rgb = max(outColor.rgb, texColor.rgb * worldAmbientColor);

    
    vec3 fogColor = vec3(1) - pow(vec3(1) - skyAmbientColor, vec3(2));
    fogColor = getFogColor(fogColor, blocklight.rgb, u_fogDensity, worldPos, cameraPosition);
    outColor.rgb = applyFog(fogColor, outColor.rgb, u_fogDensity, worldPos, cameraPosition);


    float gamma = 1.1;//1.5;
    outColor.rgb = pow(outColor.rgb, vec3(1.0/gamma));
}