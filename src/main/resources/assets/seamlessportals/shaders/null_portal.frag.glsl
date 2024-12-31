#version 150

uniform vec4 overlayColor;

out vec4 outColor;

void main() {
    vec4 resultingColor = vec4(1.0, 1.0, 1.0, 1.0);
    resultingColor.rgb = mix(resultingColor.rgb, overlayColor.rgb, overlayColor.a);

    outColor = resultingColor;
}