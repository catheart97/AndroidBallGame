# version 300 es

uniform mat4 uProjection;
uniform mat4 uModel;
uniform mat4 uView;
uniform vec4 uColor;
uniform vec3 uLightPos;
uniform float uLightning;
uniform float uAmbientStrength;

in vec3 pos;
in vec3 normal;
in vec3 lightPos;

out vec4 frag_color;

const vec3 LIGHT_COLOR = vec3(1.,1.,1.);
const float SPECULAR_STRENGTH = 0.5;

void main(void)
{
    if (uLightning == 1.0)
    {
        vec3 lightDir = normalize(lightPos - pos);
        vec3 viewDir = normalize(-pos);

        float ambient = uAmbientStrength;

        float diffuse = dot(normalize(normal), lightDir);
        if (diffuse < 0.0) diffuse = 0.0;

        float d = length(lightPos - pos);
        float quadratic = 1.8; float linear = .5; float constant = .2;
        float attenuation = 1.0 / (quadratic*d*d + linear*d + constant);
//        attenuation = 1.0;

        float intensity = dot(normalize(lightDir + viewDir), normalize(normal));
        if (intensity < 0.0) intensity = 0.0;
        float specular = pow(intensity, 20.0) * SPECULAR_STRENGTH;

        frag_color = vec4((ambient + (diffuse + specular)) * attenuation * LIGHT_COLOR * uColor.xyz, uColor.w);
    }
    else
    {
        frag_color = vec4(uAmbientStrength * uColor.xyz, 1.0);
    }
}
