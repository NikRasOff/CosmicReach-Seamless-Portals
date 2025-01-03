#version 150

#ifdef GL_ES
precision mediump float;
#endif

uniform vec2 screenSize;
uniform sampler2D screenTex;
uniform vec4 overlayColor;

uniform vec2 u_portalSize;
uniform sampler2D u_noiseTex;
uniform vec4 u_outlineColor;
uniform int u_time;

in vec2 v_texCoord0;

out vec4 outColor;

void main() {
    // A really shitty shader that hopefully produces a cool effect
    vec4 resultingColor = texture(screenTex, gl_FragCoord.xy / screenSize);

    vec2 outline_coord = v_texCoord0 * u_portalSize.yx / 2;
    vec4 initial_color = texture(u_noiseTex, outline_coord);
    outline_coord.x += u_time * 0.03125 + int(initial_color.r * 32);
    outline_coord.y += u_time * 0.03125 + int(initial_color.g * 32);
    vec4 final_color = u_outlineColor;
    final_color.a *= min(texture(u_noiseTex, outline_coord).b + 0.5, 1);
    float dist_to_edge = min(u_portalSize.y / 2 - abs(v_texCoord0.x - 0.5) * u_portalSize.y, u_portalSize.x / 2 - abs(v_texCoord0.y - 0.5) * u_portalSize.x);
    float a = 1 - max(0, 0.125 - dist_to_edge);
    final_color.a *= max(0, 1 - a * a * a);

    resultingColor.rgb = mix(resultingColor.rgb, final_color.rgb, final_color.a);

    resultingColor.rgb = mix(resultingColor.rgb, overlayColor.rgb, overlayColor.a);

    outColor = resultingColor;
}