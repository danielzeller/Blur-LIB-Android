uniform mat4 u_Matrix;

attribute vec4 a_Position;  
attribute vec2 a_TextureCoordinates;
attribute vec2 a_TextureCoordinates2;

varying vec2 v_TextureCoordinates;
varying vec2 v_TextureCoordinates2;

void main()                    
{                            
    v_TextureCoordinates = a_TextureCoordinates;
    v_TextureCoordinates2 = a_TextureCoordinates2;
    gl_Position = u_Matrix * a_Position;    
}          