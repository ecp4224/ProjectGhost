#version 330

in vec4 fragColor;
in vec2 fragTexture;

uniform vec4 uColor;

uniform sampler2D sampler;

uniform float uAmbientPower;
uniform vec3 uAmbientColor;

void main() {
	vec4 ambient = uAmbientPower * vec4(uAmbientColor, 1.0);
	
	gl_FragColor = uColor * ambient * texture(sampler, fragTexture);
}