#version 150

in vec3 worldPos;

uniform vec2 screenSize;
uniform sampler2D screenTex;

out vec4 outColor;

void main() {
    outColor = texture(screenTex, gl_FragCoord.xy / screenSize);
}