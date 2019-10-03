#version 400

in vec4 oColor;
in vec3 oTexcoord;

out vec4 FragColor;

uniform samplerCube texSampler;

void main() {
    FragColor = texture(texSampler, oTexcoord);
}