using System.Collections.Generic;
using Newtonsoft.Json;

namespace MapCreator.App
{
    public class TextureData
    {
        [JsonProperty("name")]
        public string Name { get; private set; }

        [JsonProperty("path")]
        public string Path { get; private set; }

        [JsonProperty("id")]
        public short Id { get; private set; }

        //This code is cancer, but oh well...
        [JsonProperty("extras")]
        public Dictionary<string, string> DefaultExtras = new Dictionary<string, string>(); 

        public override string ToString()
        {
            return Name ?? "Unnamed object";
        }
    }
}
