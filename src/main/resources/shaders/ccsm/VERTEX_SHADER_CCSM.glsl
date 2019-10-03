#version 400

layout(location=0) in vec3 position;
layout(location=1) in vec2 texcoord;
layout(location=2) in vec3 normal;
layout(location=3) in vec3 color;

uniform mat4 mvp;
uniform mat4 viewMatrix;

out VSOUT {
    vec3 fragPos;
    vec3 normal;
    vec2 texcoord;
} vsOut;
out vec3 oColor;
out vec4 v2fPosition;
out float clipZ;

void main() {
    oColor = color;
    vsOut.fragPos = position;
    vsOut.normal = normal;
    vsOut.texcoord = texcoord;
    v2fPosition = viewMatrix * vec4(position, 1.0);

    gl_Position = mvp * vec4(position, 1.0);

    clipZ = gl_Position.z;
}