// ─── Backdrop Blur Fragment Shader ───
// Performs a hardware-accelerated 2D Gaussian/Box approximation blur pass

uniform sampler2D u_texture;
uniform vec2 u_texelSize;
uniform float u_offset;

varying vec2 v_texCoord0;

void main() {
    vec2 uv = v_texCoord0;
    vec2 hp = u_texelSize * u_offset;
    
    // 5-tap diagonal box filter with center weighting
    vec4 sum = texture2D(u_texture, uv) * 4.0;
    sum += texture2D(u_texture, uv + vec2(-hp.x, -hp.y));
    sum += texture2D(u_texture, uv + vec2( hp.x, -hp.y));
    sum += texture2D(u_texture, uv + vec2(-hp.x,  hp.y));
    sum += texture2D(u_texture, uv + vec2( hp.x,  hp.y));
    
    gl_FragColor = sum / 8.0;
}
