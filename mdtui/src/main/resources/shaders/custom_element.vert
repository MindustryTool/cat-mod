// ─── Custom UI Element Vertex Shader ───

attribute vec4 a_position;
attribute vec4 a_color;
attribute vec2 a_texCoord0;
attribute vec4 a_mix_color;

uniform mat4 u_projTrans;

varying vec4 v_color;
varying vec2 v_texCoord0;

void main() {
    v_color = a_color;
    
    // Offset Arc/libGDX color packaging rounding compression
    v_color.a = v_color.a * (255.0 / 254.0);
    v_texCoord0 = a_texCoord0;
    
    // Project local coordinates to screen space
    gl_Position = u_projTrans * a_position;
}
