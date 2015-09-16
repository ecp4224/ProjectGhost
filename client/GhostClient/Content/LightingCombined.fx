float ambient;
float4 ambientColor;
float lightAmbient;
 
sampler ColorMapSampler : register(s0);
sampler ShadingMapSampler : register(s1)
{
	Texture = (ShadingMap);
	Filter = Linear;
	AddressU = clamp;
	AddressV = clamp;
};
 
float4 CombinedPixelShader(float4 position : SV_Position, float4 color : COLOR0, float2 texCoord : TEXCOORD0) : COLOR0
{
    float4 color2 = tex2D(ColorMapSampler, texCoord);
    float4 shading = tex2D(ShadingMapSampler, texCoord);
 
    float4 finalColor = (color2 * ambientColor * ambient);
 
    finalColor += (color2 * shading * lightAmbient);
 
    return finalColor;
}
 
technique DeferredCombined
{
    pass Pass1
    {
        PixelShader = compile ps_4_0_level_9_1 CombinedPixelShader();
    }
}