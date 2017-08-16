precision mediump float;

varying vec2 v_TextureCoordinates;

uniform sampler2D y_texture;
uniform sampler2D uv_texture;

void main() {
    float r, g, b, y, u, v;

    y = texture2D(y_texture, v_TextureCoordinates).r;
    u = texture2D(uv_texture, v_TextureCoordinates).a - 0.5;
    v = texture2D(uv_texture, v_TextureCoordinates).r - 0.5;

    r = y + 1.13983 * v;
    g = y - 0.39465 * u - 0.58060 * v;
    b = y + 2.03211 * u;

    gl_FragColor = vec4(r, g, b, 1.0);
}