#version 150

in vec3 a_position;

uniform mat4 u_projViewTrans;
uniform mat4 transMatrix;
uniform vec3 localOffset;
uniform vec3 portScale;

out vec3 worldPos;

void main(){
    vec4 tmp = transMatrix * vec4((a_position * portScale + localOffset), 1.0);
    worldPos = tmp.xyz;
    gl_Position = (u_projViewTrans * vec4(worldPos, 1.0));
}