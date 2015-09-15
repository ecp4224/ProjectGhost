float ambient;
float4 ambientColor;
float lightAmbient;
 
Texture ColorMap;
sampler ColorMapSampler = sampler_state {
    texture = <ColorMap>;
    magfilter = LINEAR;
    minfilter = LINEAR;
    mipfilter = LINEAR;
    AddressU = mirror;
    AddressV = mirror;
};
 
Texture ShadingMap;
sampler ShadingMapSampler = sampler_state {
    texture = <ShadingMap>;
    magfilter = LINEAR;
    minfilter = LINEAR;
    mipfilter = LINEAR;
    AddressU = mirror;
    AddressV = mirror;
};
 
float4 CombinedPixelShader(float4 color : COLOR0, float2 texCoords : TEXCOORD0) : COLOR0
{
    float4 color2 = tex2D(ColorMapSampler, texCoords);
    float4 shading = tex2D(ShadingMapSampler, texCoords);
 
    float4 finalColor = (color2 * ambientColor * ambient);
 
    finalColor += color2 * shading * lightAmbient;
 
    return finalColor;
}
 
technique DeferredCombined
{
    pass Pass1
    {
        PixelShader = compile ps_4_0_level_9_1 CombinedPixelShader();
    }
}