attribute vec4 a_Position;
attribute vec2 a_TextureCoordinates;

uniform mat4 uMVPMatrix;

varying vec2 v_TextureCoordinates;

void main() {
    gl_Position =  uMVPMatrix * a_Position;
    v_TextureCoordinates = a_TextureCoordinates;
}