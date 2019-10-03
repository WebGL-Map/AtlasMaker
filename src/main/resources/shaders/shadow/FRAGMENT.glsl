#version 400

in vec2 oTexcoord;
uniform sampler2D textureMap;

void main(){
    if(texture(textureMap, oTexcoord).a < 0.5) {
        discard;
    }
}