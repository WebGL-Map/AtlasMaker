#version 400

layout(location=0) in vec3 position;

uniform mat4 mvp;

out vec3 oTexcoord;

void main() {
    oTexcoord = position;
    vec4 pos = mvp * vec4(position, 1.0);
    gl_Position = pos.xyww;
}