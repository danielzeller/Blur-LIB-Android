#extension GL_OES_EGL_image_external : require
precision mediump float;

uniform sampler2D mainTexture;
varying vec2 v_TextureCoordinates;

void main()
{
    gl_FragColor = texture2D(mainTexture, v_TextureCoordinates);
}