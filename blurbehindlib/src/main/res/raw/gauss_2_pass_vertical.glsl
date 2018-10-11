#extension GL_OES_EGL_image_external : require
precision mediump float;


varying vec2 v_TextureCoordinates;
uniform sampler2D u_TextureUnit;
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
    vec4 sampleTex;
    vec4 col;
    float weightSum = 0.0;

    int step = int(float( blurRadius/15)*scale)+1;

    for(int i = 0; i < diameter; i+=step) {
        vec2 offset = vec2(float(i - blurRadius) * uWidthOffset,  float(i - blurRadius) * uHeightOffset);
        sampleTex = vec4(texture2D(u_TextureUnit, v_TextureCoordinates.st+offset));
        float index = float(i);
        float gaussWeight = getGaussWeight(index - float(diameter - 1)/2.0,  (float(diameter - 1)/2.0 + 1.0) / 2.0);
        col += sampleTex * gaussWeight;
        weightSum += gaussWeight;
    }

    gl_FragColor = col / weightSum;
}