#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP 
#endif

uniform sampler2D u_texture;
uniform vec2 resolution;
varying LOWP vec4 vColour;
varying vec2 vTexCoord;


vec2 lensDistort(vec2 c, float off)
{
	float distortion = 1.9;
	vec2 cc = c;
	cc.x=cc.x - off;
	cc.y=cc.y - 0.5;
	float dist = dot(cc, cc) * distortion;
	vec2 r = (c + cc * (1.0 + dist) );
	return r;
}

const int ignore = 0;

void main() {
	if ( ignore == 1 ) {
		gl_FragColor = vec4(texture2D(u_texture, vTexCoord).rgb,1.0);
		return;
	}

	float zoom = 0.5;
	vec2 uv;

	if ( vTexCoord.x < 0.5 ) {
		float off= 0.25;
        uv = lensDistort(vTexCoord,off);
	
        uv.t = 0.5 + (uv.t - 0.5)*(zoom);
        uv.s = off + (uv.s - off)*(zoom);

		if(uv.s <0.0 || uv.s>= 0.49 || uv.t < 0.0 || uv.t > 1.0) {
     			gl_FragColor = vec4(0.0,0.0,0.0,1.0);
      			return;
		}
	}  else {
		float off= 0.75;
        uv = lensDistort(vTexCoord,off);
	
        uv.t = 0.5 + (uv.t - 0.5)*(zoom);
        uv.s = off + (uv.s - off)*(zoom);

		if(uv.s <=0.51 || uv.s> 1.0 || uv.t < 0.0 || uv.t > 1.0) {
     			gl_FragColor = vec4(0.0,0.0,0.0,1.0);
      			return;
		}
	}

	vec4 texColour = vec4(texture2D(u_texture, uv).rgb,1.0);
		
	vec2 position = (gl_FragCoord.xy / resolution.xy) - vec2(0.5);
	
	float len = length(position);
	
	gl_FragColor = texColour * vColour;
}