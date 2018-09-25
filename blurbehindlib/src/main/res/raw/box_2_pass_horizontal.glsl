#extension GL_OES_EGL_image_external : require
precision highp float;

varying vec2 v_TextureCoordinates;
uniform samplerExternalOES u_TextureUnit;
uniform int blurRadius;


uniform float uWidthOffset;
uniform float uHeightOffset;

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
       vec2 offset = vec2(float(i - blurRadius) * uWidthOffset, float(i - blurRadius) * uHeightOffset);
       sampleTex = vec3(texture2D(u_TextureUnit, flippedYUV.st+offset));
       float index = float(i);
       float boxWeight = float(1.0) / float(diameter);
       col += sampleTex * boxWeight;
       weightSum += boxWeight;
    }
   gl_FragColor = vec4(col / weightSum, 1.0);

}