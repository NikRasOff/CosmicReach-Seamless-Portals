#version 150

#ifdef GL_ES
precision mediump float;
#endif

uniform vec2 screenSize;
uniform sampler2D screenTex;
uniform vec4 overlayColor;

out vec4 outColor;

void main() {
    vec4 resultingColor = texture(screenTex, gl_FragCoord.xy / screenSize);
    resultingColor.rgb = mix(resultingColor.rgb, overlayColor.rgb, overlayColor.a);

    outColor = resultingColor;
}