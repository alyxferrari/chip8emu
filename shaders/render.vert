#version 330 core
layout (location = 0) in vec2 pos;
layout (location = 1) in vec3 color;
uniform vec2 dim;
out vec4 col;
void main() {
    float x = ((pos.x/dim.x)*2.0)-1.0;
    float y = -((pos.y/dim.y)*2.0)-1.0;
    gl_Position = vec4(x, y, 0.0, 1.0);
    col = vec4(color, 1.0);
}
