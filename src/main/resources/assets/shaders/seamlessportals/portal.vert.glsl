#version 150

in vec3 a_position;

uniform mat4 u_projViewTrans;
uniform vec3 posOffset;
uniform vec3 localOffset;
uniform vec3 portScale;

out vec3 worldPos;

void main(){
    worldPos = (a_position * portScale + localOffset) + posOffset;
    gl_Position = (u_projViewTrans * vec4(worldPos, 1.0));
}