# version 310 es

layout(location = 0) in vec3 vpos;

uniform mat4 uProjection;
uniform mat4 uModel;
uniform mat4 uView;
uniform vec4 uColor;
uniform vec3 uLightPos;

out vec3 pos;
out vec3 normal;
out vec3 lightPos;

void main(void)
{
    vec4 position = uView * uModel * vec4(vpos, 1);
    gl_Position = uProjection * position;
    gl_PointSize = 10.f;
    pos = position.xyz / position.w;
    normal = normalize(vec3(transpose(inverse(uView * uModel)) * vec4(normalize(vpos), 1)));
    lightPos = vec3(uView * vec4(uLightPos, 1));
}
