#version 150

in vec2 a_boneWeight0;
in vec2 a_boneWeight1;
in vec2 a_boneWeight2;
in vec2 a_boneWeight3;
in vec3 a_position;
in vec2 a_texCoord0;

uniform mat4 u_worldTrans;
uniform mat4 u_projTrans;
uniform mat4 u_bones[4];

out vec2 v_texCoord0;
out vec3 v_worldPos;

void main() {
    mat4 skinning = mat4(0.0);
    skinning += (a_boneWeight0.y) * u_bones[int(a_boneWeight0.x)];
    skinning += (a_boneWeight1.y) * u_bones[int(a_boneWeight1.x)];
    skinning += (a_boneWeight2.y) * u_bones[int(a_boneWeight2.x)];
    skinning += (a_boneWeight3.y) * u_bones[int(a_boneWeight3.x)];

    v_texCoord0 = a_texCoord0;
    vec4 worldPos = u_worldTrans * skinning * vec4(a_position, 1.0);
    v_worldPos = worldPos.xyz;
    gl_Position = u_projTrans * worldPos;
}
