float screenWidth;
float screenHeight;
 
float lightStrength;
float lightRadius;
float3 lightPosition;
float3 lightColor;
 
Texture NormalMap;
sampler NormalMapSampler = sampler_state {
    texture = <NormalMap>;
    magfilter = LINEAR;
    minfilter = LINEAR;
    mipfilter = LINEAR;
    AddressU = mirror;
    AddressV = mirror;
};
 
Texture DepthMap;
sampler DepthMapSampler = sampler_state {
    texture = <DepthMap>;
    magfilter = LINEAR;
    minfilter = LINEAR;
    mipfilter = LINEAR;
    AddressU = mirror;
    AddressV = mirror;
};
 
struct VertexToPixel
{
	float4 Position : SV_POSITION;
    float2 TexCoord : TEXCOORD0;
};
 
struct PixelToFrame
{
    float4 Color : COLOR0;
};
 
VertexToPixel VertexToPixelShader(float4 inPos: SV_POSITION, float2 texCoord : TEXCOORD0)
{
    VertexToPixel Output = (VertexToPixel)0;
 
    Output.Position = inPos;
    Output.TexCoord = texCoord;
 
    return Output;
}
 
PixelToFrame PointLightShader(VertexToPixel PSIn) : COLOR0
{
    PixelToFrame Output = (PixelToFrame)0;
 
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
    if (depth > 0)
    {
        float3 lightDirection = lightPosition - pixelPosition;
        float distance = 1 / length(lightPosition - pixelPosition) * lightStrength;
        float amount = max(dot(normal + depth, normalize(distance)), 0);
 
                float coneAttenuation = saturate(1.0f - length(lightDirection) / lightRadius);
 
        shading = distance * amount * coneAttenuation * lightColor;
    }
 
    Output.Color = float4(shading.r, shading.g, shading.b, 1.0f);
    return Output;
}
 
technique DeferredPointLight
{
    pass Pass1
    {
		VertexShader = compile vs_4_0_level_9_1 VertexToPixelShader();
        PixelShader = compile ps_4_0_level_9_1 PointLightShader();
    }
}
