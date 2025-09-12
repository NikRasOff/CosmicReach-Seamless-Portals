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

    float scrollSpeed = 0.05;
    vec2 tileSize = vec2(1.0/64.0, 1.0/64.0);
    vec2 tileOrigin = floor(v_texCoord0 / tileSize) * tileSize;
    vec2 localUV = vec2(fract((v_texCoord0.x - tileOrigin.x) / tileSize.x), (v_texCoord0.y - tileOrigin.y) / tileSize.y);
    //localUV =  fract(localUV + u_time * scrollSpeed);
    vec2 atlasUV = tileOrigin + localUV * tileSize;
    vec4 texColor = texture(texDiffuse, atlasUV);

    vec4 la = texture(texDiffuse, vec2(atlasUV.x, tileOrigin.y + tileSize.y * fract(localUV.y + sin(u_time) * scrollSpeed)));
    vec4 lb = texture(texDiffuse, vec2(atlasUV.x, tileOrigin.y + tileSize.y * fract(localUV.y + cos(u_time) * scrollSpeed)));
    vec4 lc = texture(texDiffuse, vec2(tileOrigin.x + tileSize.x * fract(localUV.x + sin(u_time) * scrollSpeed), atlasUV.y));
    vec4 ld = texture(texDiffuse, vec2(tileOrigin.x + tileSize.x * fract(localUV.x + cos(u_time) * scrollSpeed), atlasUV.y));
    
    texColor = mix(texColor, max(max(la, lb), max(lc, ld)), 0.25 + 0.25*sin(u_time * 0.2));

    vec3 lightTint = max(blocklight.rgb, blocklight.a * skyAmbientColor);
    float alpha = 1.0;
    
    float fadeOutDistance = (u_renderDistanceInChunks - 1) * 16;
    float fadeOutFactor = clamp((fadeOutDistance - length(worldPos.xz - cameraPosition.xz))/16.0, 0, 1);
    alpha = alpha * pow(fadeOutFactor, 0.5);
    if(alpha == 0)
    {
        discard;
    }

    outColor = vec4(texColor.rgb * lightTint, alpha);
    outColor *= tintColor;
    outColor.rgb = max(outColor.rgb, texColor.rgb * worldAmbientColor);

    vec3 fogColor = vec3(1) - pow(vec3(1) - skyAmbientColor, vec3(2));
    fogColor = getFogColor(fogColor, blocklight.rgb, u_fogDensity, worldPos, cameraPosition);
    outColor.rgb = applyFog(fogColor, outColor.rgb, u_fogDensity, worldPos, cameraPosition);

    float gamma = 1.1;//1.5;
    outColor.rgb = pow(outColor.rgb, vec3(1.0/gamma));
}
