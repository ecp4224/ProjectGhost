float screenWidth;
float screenHeight;
 
float lightStrength;
float lightRadius;
float3 lightPosition;
float3 lightColor;

sampler NormalMapSampler : register(s0)
{
	Texture = (NormalMap);
	Filter = Linear;
	AddressU = clamp;
	AddressV = clamp;
};
sampler DepthMapSampler : register(s1)
{
	Texture = (DepthMap);
	Filter = Linear;
	AddressU = clamp;
	AddressV = clamp;
};

// Vertex shader input structure
struct VertexShaderInput
{
	float4 Pos : SV_Position;
    float2 TexCoord : TEXCOORD0;
};

// Vertex shader output structure
struct VertexShaderOutput
{
	float4 Pos : SV_Position;
    float2 TexCoord : TEXCOORD0;
};
 
VertexShaderOutput VertexToPixelShader(VertexShaderInput input)
{
    VertexShaderOutput output;
 
	output.Pos = input.Pos;
    output.TexCoord = input.TexCoord;
 
    return output;
}
 
float4 PointLightShader(VertexShaderOutput PSIn) : COLOR0
{
    float3 normal = tex2D(NormalMapSampler, PSIn.TexCoord).rgb;
    normal = normal*2.0f-1.0f;
    normal = normalize(normal);
 
    float depth = tex2D(DepthMapSampler, PSIn.TexCoord);
 
    float3 pixelPosition;
    pixelPosition.x = screenWidth * PSIn.TexCoord.x;
    pixelPosition.y = screenHeight * PSIn.TexCoord.y;
    pixelPosition.z = depth;
    //pixelPosition.w = 1.0f;
 
    float3 shading;
    //if (depth > 0)
    //{
        float3 lightDirection = lightPosition - pixelPosition;
        float distance = 1.0f / length(lightPosition - pixelPosition) * lightStrength;
        float amount = max(dot(normal + depth, normalize(distance)), 0);
 
                float coneAttenuation = saturate(1.0f - length(lightDirection) / lightRadius);
 
        shading = distance * amount * coneAttenuation * lightColor;
    //}

    return float4(shading.r, shading.g, shading.b, 1.0f);
}
 
technique DeferredPointLight
{
    pass Pass1
    {
		VertexShader = compile vs_4_0_level_9_1 VertexToPixelShader();
        PixelShader = compile ps_4_0_level_9_1 PointLightShader();
    }
}
