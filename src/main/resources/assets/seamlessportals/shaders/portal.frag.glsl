#ifdef GL_ES
precision mediump float;
#endif

uniform vec2 screenSize;
uniform sampler2D screenTex;
uniform vec4 overlayColor;

void main() {
    vec4 resultingColor = texture(screenTex, gl_FragCoord.xy / screenSize);
    resultingColor.r = resultingColor.r * (1 - overlayColor.a) + overlayColor.r * overlayColor.a;
    resultingColor.g = resultingColor.g * (1 - overlayColor.a) + overlayColor.g * overlayColor.a;
    resultingColor.b = resultingColor.b * (1 - overlayColor.a) + overlayColor.b * overlayColor.a;

    gl_FragColor = resultingColor;
}