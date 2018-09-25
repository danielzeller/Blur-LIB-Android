#extension GL_OES_EGL_image_external : require
precision highp float;


varying vec2 v_TextureCoordinates;
uniform samplerExternalOES u_TextureUnit;
uniform float blurRadius;


uniform float uWidthOffset;
uniform float uHeightOffset;
uniform float scale;

#define max_loop_step 40.0
#define step_interp 0.1
mediump float getGaussWeight(mediump float currentPos, mediump float sigma)
{
    return 1.0 / sigma * exp(-(currentPos * currentPos) / (2.0 * sigma * sigma));
}

float getInterpolation(float idata) {
    return  idata * idata * idata * idata;
}

void main() {
    float diameter = 2.0 * blurRadius + 1.0;
    vec3 sampleTex;
    vec3 col;
    float weightSum = 0.0;
    vec2 flippedYUV=v_TextureCoordinates;
    flippedYUV.y=1.0-flippedYUV.y;

    float maxStep = max_loop_step*scale;
    float currentStep=max(((blurRadius+ 1.0)*step_interp)*scale,1.0);
//    currentStep=getInterpolation(currentStep/maxStep)*maxStep;
    float step = min(currentStep , maxStep);

    for(float i = 0.0; i < diameter; i+=step) {
       vec2 offset = vec2((i - blurRadius) * uWidthOffset,  (i - blurRadius) * uHeightOffset);
       sampleTex = vec3(texture2D(u_TextureUnit, flippedYUV.st+offset));
       float index = i;
       float gaussWeight = getGaussWeight(index - (diameter - 1.0)/2.0,  ((diameter - 1.0)/2.0 + 1.0) / 2.0);
       col += sampleTex * gaussWeight;
       weightSum += gaussWeight;
    }
    gl_FragColor = vec4(col / weightSum, 1.0);
}