using Newtonsoft.Json;

namespace MapCreator.App
{
    public class Color3
    {
        [JsonProperty("red")]
        public float R { get; private set; }

        [JsonProperty("green")]
        public float G { get; private set; }

        [JsonProperty("blue")]
        public float B { get; private set; }

        public Color3(float r, float g, float b)
        {
            R = r;
            G = g;
            B = b;
        }
    }
}
