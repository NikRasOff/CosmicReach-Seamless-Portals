#version 150

uniform vec2 screenSize;
uniform sampler2D screenTex;
uniform vec4 overlayColor;

out vec4 outColor;

void main() {
    vec4 resultingColor = texture(screenTex, gl_FragCoord.xy / screenSize);
    resultingColor.r = resultingColor.r * (1 - overlayColor.a) + overlayColor.r * overlayColor.a;
    resultingColor.g = resultingColor.g * (1 - overlayColor.a) + overlayColor.g * overlayColor.a;
    resultingColor.b = resultingColor.b * (1 - overlayColor.a) + overlayColor.b * overlayColor.a;

    outColor = resultingColor;
}