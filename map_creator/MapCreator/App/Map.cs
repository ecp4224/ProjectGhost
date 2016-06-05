using System;
using System.Collections.Generic;
using System.IO;
using MapCreator.Render.Sprite;
using Newtonsoft.Json;

namespace MapCreator.App
{
    public sealed class Map
    {
        [JsonProperty("name")]
        public string Name { get; set; }

        [JsonProperty("backgroundTexture")]
        public string BackgroundTexture { get; set; }

        [JsonProperty("locations")]
        public List<MapObject> Entities { get; set; }

        [JsonProperty("ambientPower")]
        public float AmbientPower { get; set; }

        [JsonProperty("ambientColor")]
        public Color3 AmbientColor { get; set; }

        [JsonIgnore]
        public string Json
        {
            get
            {
                //Convert degrees to radians
                Entities.ForEach(e => e.Rotation = e.Rotation/(180.0/Math.PI));

                string json = JsonConvert.SerializeObject(this);

                //Convert back
                Entities.ForEach(e => e.Rotation = e.Rotation * (180.0 / Math.PI));

                return json;
            }
        }

        public Map()
        {
            Name = "whatever";
            BackgroundTexture = "null";
            AmbientPower = 1f;
            AmbientColor = new Color3(255, 255, 255);
            Entities = new List<MapObject>();
        }

        public static Map Create(string fileName)
        {
            var map = JsonConvert.DeserializeObject<Map>(File.ReadAllText(fileName));
            map.Entities.ForEach(e => e.Init());

            return map;
        }
    }
}
