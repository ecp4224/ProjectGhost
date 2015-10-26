using System;
using Newtonsoft.Json;

namespace MapCreator.App
{
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
                //TODO: actually figure out a way to deal with precission loss
                _rotation = Math.Truncate(value * 10000.0) / 10000.0;
            }
        }

        public Entity()
        {
            Console.WriteLine(Id + " " + X + " " + Y + " " + Rotation);
        }
    }
}
