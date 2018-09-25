precision highp float;

varying vec2 v_TextureCoordinates;
uniform sampler2D u_TextureUnit;
uniform int blurRadius;

uniform float uWidthOffset;
uniform float uHeightOffset;


void main() {
   int diameter = 2 * blurRadius + 1;
     vec3 sampleTex;
     vec3 col;
     float weightSum = 0.0;
     vec2 flippedYUV=v_TextureCoordinates;

     int step = int(float(blurRadius)*0.2);
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