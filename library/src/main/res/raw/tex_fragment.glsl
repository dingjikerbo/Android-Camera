precision mediump float;

varying vec2 v_TextureCoordinates;

uniform sampler2D s_texture;

void main() {
    gl_FragColor = texture2D(s_texture, v_TextureCoordinates);

//    float r = texture2D(s_texture, v_TextureCoordinates).r;
//    float g = texture2D(s_texture, v_TextureCoordinates).g;
//    float b = texture2D(s_texture, v_TextureCoordinates).b;
//    gl_FragColor = vec4(1.0, g, b, 1.0);
}