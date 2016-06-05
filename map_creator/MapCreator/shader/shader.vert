#version 330

layout(location = 0) in vec2 vertPosition;
layout(location = 1) in vec2 vertTexture;

out vec2 fragTexture;

uniform mat4 pMatrix;
uniform mat4 mvMatrix;

void main() {
	gl_Position = pMatrix * mvMatrix * vec4(vertPosition, 0.1, 1.0);
	fragTexture = vec2(vertTexture.x, 1.0 - vertTexture.y);
}
