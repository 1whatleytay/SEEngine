#version 120

attribute vec2 position;
attribute float texIndex;

uniform vec2 crd;
uniform mat2 execMat;
uniform vec2 texArray[4];

varying vec2 out_texCoord;

void main() {
	out_texCoord = texArray[int(texIndex)];
	gl_Position = vec4(execMat * position + crd, 0, 1);
}