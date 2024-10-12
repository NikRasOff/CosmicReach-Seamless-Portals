#version 150

uniform vec4 overlayColor;

void main() {
    vec4 resultingColor = vec4(1.0, 1.0, 1.0, 1.0);
    resultingColor.r = resultingColor.r * (1 - overlayColor.a) + overlayColor.r * overlayColor.a;
    resultingColor.g = resultingColor.g * (1 - overlayColor.a) + overlayColor.g * overlayColor.a;
    resultingColor.b = resultingColor.b * (1 - overlayColor.a) + overlayColor.b * overlayColor.a;

    gl_FragColor = resultingColor;
}