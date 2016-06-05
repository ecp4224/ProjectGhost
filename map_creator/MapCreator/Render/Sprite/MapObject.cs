using System.Collections.Generic;
using System.Drawing;
using Newtonsoft.Json;

namespace MapCreator.Render.Sprite
{
    public sealed class MapObject : Sprite
    {
        [JsonProperty("id")]
        public new short Id
        {
            get { return base.Id; }
            set { base.Id = value; }
        }

        [JsonProperty("x")]
        public new float X {
            get { return base.X; }
            set { base.X = value; }
        }

        [JsonProperty("y")]
        public new float Y
        {
            get { return base.Y; }
            set { base.Y = value; }
        }

        [JsonProperty("width")]
        public new float Width
        {
            get { return base.Width; }
            set { base.Width = value; }
        }

        [JsonProperty("height")]
        public new float Height
        {
            get { return base.Height; }
            set { base.Height = value; }
        }

        [JsonProperty("rotation")]
        public new double Rotation
        {
            get { return base.Rotation; }
            set { base.Rotation = value; }
        }

        [JsonProperty("color")]
        public int[] JsonTint
        {
            get { return new int[] {Tint.R, Tint.G, Tint.B, Tint.A}; }
            set { Tint = Color.FromArgb(value[3], value[0], value[1], value[2]); }
        }

        [JsonProperty("extras")]
        public Dictionary<string, string> ExtraData { get; set; }

        private bool _selected;
        public bool Selected
        {
            get { return _selected; }
            set
            {
                _selected = value;
                Color = value ? Color.LightSalmon : Color.White;
            }
        }

        public MapObject(short id, string name = "Sprite") : base(id, name)
        {
            ExtraData = Texture.GetAssociatedData(id);
        }

        public override string ToString()
        {
            return Name ?? "Sprite";
        }
    }
}
