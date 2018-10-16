#extension GL_OES_EGL_image_external : require
precision mediump float;


varying vec2 v_TextureCoordinates;
uniform samplerExternalOES mainTexture;
uniform int blurRadius;


uniform float textureWidth;
uniform float textureHeight;
uniform float scale;

mediump float getGaussWeight(mediump float currentPos, mediump float sigma)
{
    return 1.0 / sigma * exp(-(currentPos * currentPos) / (2.0 * sigma * sigma));
}

void main() {
    int diameter = 2 * blurRadius + 1;
    vec4 sampleTex;
    vec4 col;
    float weightSum = 0.0;
    vec2 flippedYUV = v_TextureCoordinates;
    flippedYUV.y = 1.0 - flippedYUV.y;

    int step = int(float( blurRadius/15)*scale)+1;

    for(int i = 0; i < diameter; i+=step) {
       vec2 offset = vec2(float(i - blurRadius) * textureWidth,  float(i - blurRadius) * textureHeight);
       sampleTex = vec4(texture2D(mainTexture, flippedYUV.st+offset));
       float index = float(i);
       float gaussWeight = getGaussWeight(index - float(diameter - 1)/2.0,  (float(diameter - 1)/2.0 + 1.0) / 2.0);
       col += sampleTex * gaussWeight;
       weightSum += gaussWeight;
    }

    gl_FragColor =  vec4(col.rgb / weightSum,1.0);
}