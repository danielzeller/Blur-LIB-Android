#extension GL_OES_EGL_image_external : require
precision highp float;


varying vec2 v_TextureCoordinates;
uniform samplerExternalOES u_TextureUnit;
uniform int blurRadius;


uniform float uWidthOffset;
uniform float uHeightOffset;
uniform float scale;

mediump float getGaussWeight(mediump float currentPos, mediump float sigma)
{
    return 1.0 / sigma * exp(-(currentPos * currentPos) / (2.0 * sigma * sigma));
}

void main() {
    int diameter = 2 * blurRadius + 1;
    vec3 sampleTex;
    vec3 col;
    float weightSum = 0.0;
    vec2 flippedYUV=v_TextureCoordinates;
    flippedYUV.y=1.0-flippedYUV.y;

   for(int i = 0; i < diameter; i++) {
       vec2 offset = vec2(float(i - blurRadius) * uWidthOffset,  float(i - blurRadius) * uHeightOffset);
       sampleTex = vec3(texture2D(u_TextureUnit, flippedYUV.st+offset));
       float index = float(i);
       float gaussWeight = getGaussWeight(index - float(diameter - 1)/2.0,  (float(diameter - 1)/2.0 + 1.0) / 2.0);
       col += sampleTex * gaussWeight;
       weightSum += gaussWeight;
    }

    gl_FragColor = vec4(col / weightSum, 1.0);
}