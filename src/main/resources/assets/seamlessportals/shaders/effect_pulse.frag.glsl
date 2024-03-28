#version 150

in vec3 worldPos;

uniform vec4 modelColor;

out vec4 outColor;

void main() {
    outColor = modelColor;
}