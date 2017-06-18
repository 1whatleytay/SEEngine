#version 120

uniform sampler2D tex;
uniform float textureCount;
uniform float ctexture;

varying vec2 out_texCoord;

void main() {
	vec2 mtex = out_texCoord;
	mtex.y /= textureCount;
	mtex.y += (1/textureCount) * ctexture;
	gl_FragColor = texture2D(tex, mtex);
}