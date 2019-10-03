#version 400

in VSOUT {
    vec3 fragPos;
    vec3 normal;
    vec2 texcoord;
} vsOut;
in vec3 oColor;
in vec4 v2fPosition;
in float clipZ;

uniform float ambientAmount;
uniform float enableLighting;

uniform vec3 lightPos;
uniform vec3 viewPos;
uniform vec3 lightColor;

uniform sampler2D shadowMap[4];
uniform sampler2D textureMap;
uniform vec4 cascadedSplits;
uniform mat4 lightViewProjectionMatrices[4];
uniform mat4 inverseViewMatrix;


out vec4 FragColor;

float ShadowCalculation() {
    int cascadeIdx=0;
    for (int i = 3; i > 0; i--) {
        if (clipZ < cascadedSplits[i]) {
            cascadeIdx = i;
        }
    }

    // perform perspective divide
    mat4 lightViewProjectionMatrix = lightViewProjectionMatrices[cascadeIdx];
    vec2 TexCoord = gl_FragCoord.xy / vec2(1000.0f, 600.0f);
    vec4 tex_coord = vec4(TexCoord.x, TexCoord.y, cascadeIdx, 1.0);
    vec4 fragmentModelViewPosition = vec4(v2fPosition.xyz,1.0f);
    vec4 fragmentModelPosition = inverseViewMatrix * fragmentModelViewPosition;
    vec4 fragmentShadowPosition = lightViewProjectionMatrix * fragmentModelPosition;
    vec3 projCoords = fragmentShadowPosition.xyz / fragmentShadowPosition.w;

    // transform to [0,1] range
    projCoords = projCoords * 0.5 + 0.5;
    // get depth of current fragment from light's perspective

    //projCoords.y = 1.0f - projCoords.y;

    float currentDepth = projCoords.z;
    // calculate bias (based on depth map resolution and slope)
    projCoords.z = cascadeIdx;

    //float bias = max(0.006f * (1.0 - dot(normalize(fsIn.normal), normalize(-lightPos)*-1.0f)), 0.0008);
    float bias = 0.00001 * tan(acos(dot(normalize(vsOut.normal), normalize(lightPos))));//float bias = max(0.005 * (1.0 - dot(normal, lightDir)), 0.0001);
    bias = clamp(bias, 0.0, 0.001);

    /*vec3 toLightV = normalize(lightPos);
    float cosAngle = clamp(1.0f - dot(toLightV, normal), 0.0, 1.0);
    vec3 scaledNormalOffset = fsIn.normal * (cb_normalOffset * cosAngle * smTexelDimensions);
    vec4 shadowPosW = mul(float4(position + scaledNormalOffset, 1.0f), inverseViewMatrix);*/

    float closestDepth = texture(shadowMap[cascadeIdx], projCoords.xy).r;
    // check whether current frag pos is in shadow
    //float shadow = currentDepth - 0.000001 > closestDepth  ? 1.0 : 0.0;

    /*float shadow = 0.0;
    vec2 texelSize = 1.0 / textureSize(shadowMap[cascadeIdx], 0).xy;
    for(float x = -1.5; x <= 1.5; x+=1){
        for(float y = -1.5; y <= 1.5; y+=1){
            float pcfDepth = texture(shadowMap[cascadeIdx], projCoords.xy + vec2(x, y) * texelSize).r;
            shadow += currentDepth - 0.000001 > pcfDepth  ? 1.0 : 0.0;
        }
    }
    shadow /= 16.0;*/

    float shadow = 0.0;
    vec2 texelSize = 1.0 / textureSize(shadowMap[cascadeIdx], 0).xy;
    for(float x = -0.5; x <= 0.5; x+=0.5){
        for(float y = -1.5; y <= 0.5; y+=0.5){
            float pcfDepth = texture(shadowMap[cascadeIdx], projCoords.xy + vec2(x, y) * texelSize).r;
            shadow += currentDepth - 0.000001 > pcfDepth  ? 1.0 : 0.0;
        }
    }
    shadow /= 16.0;

    /*if(currentDepth > 1.0)
        shadow = 0.0;*/
    if(shadow == 1.0f && !gl_FrontFacing) {
        shadow = 1.0;
    }

    return shadow;
}

void main() {
    vec4 texColor = texture(textureMap, vsOut.texcoord) * vec4(oColor, 1.0);
    if(texColor.a <= 0.5) {
        discard;
    }
    vec3 normal = normalize(vsOut.normal);
    // ambient
    vec4 ambient = ambientAmount * texColor;
    // diffuse
    vec3 lightDir = normalize(lightPos - vsOut.fragPos);
    float diff = max(dot(lightDir, normal), 0.0);
    vec3 diffuse = diff * lightColor;
    // specular
    vec3 viewDir = normalize(viewPos - vsOut.fragPos);
    vec3 reflectDir = reflect(-lightDir, normal);
    float spec = 0.0;
    vec3 halfwayDir = normalize(lightDir + viewDir);
    spec = pow(max(dot(normal, halfwayDir), 0.0), 32.0); //64.0
    vec3 specular = spec * lightColor;
    // calculate shadow
    float shadow = (1.0 - ShadowCalculation()) * enableLighting;
    vec4 lighting = ambient + vec4(shadow * (diffuse + specular), 1.0) * texColor;
    FragColor = lighting;
}
