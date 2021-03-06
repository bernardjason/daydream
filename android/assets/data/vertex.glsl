#ifdef GL_ES
	#define PRECISION mediump
	precision PRECISION float;
#else
	#define PRECISION
#endif

attribute vec4 a_position;
attribute vec2 a_texCoord0;
attribute vec2 a_texCoord;
varying vec2 v_texCoords;

attribute vec4 a_color;
uniform mat4 u_projTrans;
 
varying vec4 vColour;
varying vec2 vTexCoord;
void main() {
	vColour = a_color;
	vTexCoord = a_texCoord0;
	gl_Position =  u_projTrans * a_position;
}
