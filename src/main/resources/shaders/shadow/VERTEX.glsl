#version 400
layout (location = 0) in vec3 position;
layout (location = 1) in vec2 texcoord;

uniform mat4 mvp;

out vec2 oTexcoord;

void main() {
    oTexcoord = texcoord;
    gl_Position = mvp * vec4(position, 1.0);
}
