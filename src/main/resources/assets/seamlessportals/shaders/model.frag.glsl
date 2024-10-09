#version 150
#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D u_diffuseTex;
uniform vec4 u_ambientLight;

in vec2 v_texCoord0;

out vec4 outColor;

void main() {
    vec4 tex = texture(u_diffuseTex, v_texCoord0);
    outColor = tex * u_ambientLight;
}