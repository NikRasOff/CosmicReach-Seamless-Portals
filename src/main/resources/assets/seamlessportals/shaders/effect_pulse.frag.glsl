#version 150

#ifdef GL_ES
precision mediump float;
#endif

uniform vec4 u_modelColor;

void main() {
    gl_FragColor = u_modelColor;
}
