float ambient;
float4 ambientColor;
float lightAmbient;
 
sampler ColorMapSampler : register(s0);
 
float4 CombinedPixelShader(float2 texCoords : TEXCOORD0) : COLOR0
{
    //float4 color2 = tex2D(ColorMapSampler, texCoords);
    //float4 shading = tex2D(ShadingMapSampler, texCoords);
 
    //float4 finalColor = (color2 * ambientColor * ambient);
 
    //finalColor += (color2 * shading * lightAmbient);
 
    //return finalColor;

	float4 color2 = tex2D(ColorMapSampler, texCoords);
	return color2;
}
 
technique DeferredCombined
{
    pass Pass1
    {
        PixelShader = compile ps_4_0_level_9_1 CombinedPixelShader();
    }
}