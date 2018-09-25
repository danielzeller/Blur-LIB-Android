#extension GL_OES_EGL_image_external : require
precision highp float;

varying vec2 v_TextureCoordinates;
uniform samplerExternalOES u_TextureUnit;
uniform sampler2D noise;
uniform float blurRadius;

#define repeats 30.0

vec3 draw(vec2 uv) {
    return texture2D(u_TextureUnit,vec2(uv.x,1.-uv.y)).rgb;
}

float grid(float var, float size) {
    return floor(var*size)/size;
}

float rand(vec2 co){
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

void main()
{
    vec2 uv = v_TextureCoordinates.xy;
    float bluramount = blurRadius*0.0015;

    vec3 blurred_image = vec3(0.0);
    for (float i = 0.0; i < repeats; i++) {
        vec2 q = vec2(cos(degrees((i/repeats)*360.0)), sin(degrees((i/repeats)*360.0))) *  (rand(vec2(i,uv.x+uv.y))+bluramount);
        vec2 uv2 = uv+q*bluramount;
        blurred_image += draw(uv2)/2.0;
        q = vec2(cos(degrees((i/repeats)*360.0)), sin(degrees((i/repeats)*360.0))) * (rand(vec2(i+2.0, uv.x+uv.y+24.0))+bluramount);
        uv2 = uv+q*bluramount;
        blurred_image += draw(uv2)/2.0;
    }
    blurred_image /= repeats;
    vec4 blurcolor = vec4(blurred_image, 1.0);
    gl_FragColor = blurcolor;
}