using Newtonsoft.Json;

namespace MapCreator.App
{
    public class Whatever //TODO: give this poor thing an actual name
    {
        [JsonProperty("name")]
        public string Name { get; private set; }

        [JsonProperty("path")]
        public string Path { get; private set; }

        [JsonProperty("id")]
        public short Id { get; private set; }

        public override string ToString()
        {
            return Name ?? "Unnamed object";
        }
    }
}
