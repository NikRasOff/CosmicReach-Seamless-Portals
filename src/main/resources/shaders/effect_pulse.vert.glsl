#version 150

in vec3 a_position;

uniform mat4 u_projViewTrans;
uniform vec3 posOffset;
uniform vec3 modelScale;

void main(){
    vec3 worldPos = (a_position * modelScale) + posOffset;
    gl_Position = (u_projViewTrans * vec4(worldPos, 1.0));
}