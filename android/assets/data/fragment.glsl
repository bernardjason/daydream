#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP 
#endif
uniform sampler2D u_texture;

//our screen resolution, set from Java whenever the display is resized
uniform vec2 resolution;

//"in" attributes from our vertex shader
varying LOWP vec4 vColor;
varying vec2 vTexCoord;

//RADIUS of our vignette, where 0.5 results in a circle fitting the screen
const float RADIUS = 0.75;

//softness of our vignette, between 0.0 and 1.0
const float SOFTNESS = 0.45;

//sepia colour, adjust to taste
const vec3 SEPIA = vec3(1.2, 1.0, 0.8); 


vec2 radialDistortion(vec2 c, float off)
{
	float distortion = 1.9;
	//vec2 cc = c - 0.5;
	vec2 cc = c;
	cc.x=cc.x - off;
	cc.y=cc.y - 0.5;
	float dist = dot(cc, cc) * distortion;
	//return (c + cc * (1.0 + dist) * dist);
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
        	uv = radialDistortion(vTexCoord,off);
	
        	uv.t = 0.5 + (uv.t - 0.5)*(zoom);
        	uv.s = off + (uv.s - off)*(zoom);

		if(uv.s <0.0 || uv.s>= 0.49 || uv.t < 0.0 || uv.t > 1.0) {
     			gl_FragColor = vec4(0.0,0.0,0.0,1.0);
      			return;
		}
	}  else {
		float off= 0.75;
        	uv = radialDistortion(vTexCoord,off);
	
        	uv.t = 0.5 + (uv.t - 0.5)*(zoom);
        	uv.s = off + (uv.s - off)*(zoom);

		if(uv.s <=0.51 || uv.s> 1.0 || uv.t < 0.0 || uv.t > 1.0) {
     			gl_FragColor = vec4(0.0,0.0,0.0,1.0);
      			return;
		}
	}
			

	vec4 texColor = vec4(texture2D(u_texture, uv).rgb,1.0);
	//	gl_FragColor = vec4(texture2D(u_texture, uv).rgb,1.0);
	//	vec4 texColor = texture2D(u_texture, vTexCoord);
		
	//1. VIGNETTE
	
	vec2 position = (gl_FragCoord.xy / resolution.xy) - vec2(0.5);
	
	float len = length(position);
	
	float vignette = smoothstep(RADIUS, RADIUS-SOFTNESS, len);
	texColor.rgb = mix(texColor.rgb, texColor.rgb * vignette, 0.5);
		
	//2. GRAYSCALE
	//convert to grayscale using NTSC conversion weights
	float gray = dot(texColor.rgb, vec3(0.299, 0.587, 0.114));
	
	//3. SEPIA
	//vec3 sepiaColor = vec3(gray) * SEPIA;
	//texColor.rgb = mix(texColor.rgb, sepiaColor, 0.75);
		
	//final colour, multiplied by vertex colour
	gl_FragColor = texColor * vColor;
}

