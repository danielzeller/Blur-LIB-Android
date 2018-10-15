#extension GL_OES_EGL_image_external : require
precision mediump float;

uniform sampler2D u_TextureUnit;
uniform samplerExternalOES maskTexture;
varying vec2 v_TextureCoordinates;

void main()
{
    vec2 maskCoordinates = v_TextureCoordinates;
    maskCoordinates.y = 1.0 - maskCoordinates.y;
    vec4 mask = texture2D(maskTexture, maskCoordinates);
    if(mask.a<0.05){
        discard;
    }
    vec4 color = texture2D(u_TextureUnit, v_TextureCoordinates);
    gl_FragColor =color;
}