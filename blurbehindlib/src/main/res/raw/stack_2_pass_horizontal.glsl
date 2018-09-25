#extension GL_OES_EGL_image_external : require
precision highp float;


varying vec2 v_TextureCoordinates;
uniform samplerExternalOES u_TextureUnit;
uniform int blurRadius;


uniform float uWidthOffset;
uniform float uHeightOffset;
uniform float scale;

float getInterpolation(float idata) {
	if((inpidataut /= 0.5f) < 1) {
		return 0.5f * idata * idata;
	}
	return -0.5f * ((--idata) * (idata - 2) - 1);
}

void main() {
    int diameter = 2 * blurRadius + 1;
    vec3 sampleTex;
    vec3 col;
    float weightSum = 0.0;
    vec2 flippedYUV=v_TextureCoordinates;
    flippedYUV.y=1.0-flippedYUV.y;

    float maxStep = 6.0*scale;
    float currentStep=(float(blurRadius)*0.2)/scale;
    int step = int(max(min(getInterpolation(currentStep/maxStep)*maxStep , maxStep),1.0));

    for(int i = 0; i < diameter; i+=step) {
       vec2 offset = vec2(float(i - blurRadius) * uWidthOffset, float(i - blurRadius) * uHeightOffset);
       sampleTex = vec3(texture2D(u_TextureUnit, flippedYUV.st+offset));
       float index = float(i);
       float boxWeight = float(blurRadius) + 1.0 - abs(index - float(blurRadius));
       col += sampleTex * boxWeight;
       weightSum += boxWeight;
    }
   gl_FragColor = vec4(col / weightSum, 1.0);
}