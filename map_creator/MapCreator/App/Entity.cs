using System;
using System.Collections.Generic;
using Newtonsoft.Json;

namespace MapCreator.App
{
    [Obsolete("Replaced by MapObject.")]
    public class Entity
    {
        [JsonProperty("id")]      
        public short Id { get; set; }

        [JsonProperty("x")]
        public float X { get; set; }

        [JsonProperty("y")]
        public float Y { get; set; }

        private double _rotation;
        [JsonProperty("rotation")]        
        public double Rotation 
        {
            get { return _rotation; }
            set
            {
                _rotation = Math.Truncate(value * 10000.0) / 10000.0;
            }
        }

        [JsonProperty("extras")]
        public Dictionary<string, string> ExtraData { get; set; } 

        public Entity()
        {
            if (ExtraData == null)
            {
                ExtraData = new Dictionary<string, string>();
            }
        }
    }
}
