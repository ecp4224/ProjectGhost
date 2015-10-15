using Newtonsoft.Json;

namespace MapCreator.App
{
    public class Whatever
    {
        [JsonProperty("name")]
        public string Name { get; private set; }

        [JsonProperty("path")]
        public string Path { get; private set; }

        [JsonProperty("id")]
        public byte Id { get; private set; }

        public override string ToString()
        {
            return Name ?? "Unnamed object";
        }
    }
}
