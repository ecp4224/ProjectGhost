using System;
using GhostClient.Core.Sharp2D_API;
using Microsoft.Xna.Framework.Graphics;

namespace Sharp2D
{
    public partial class Sprite
    {
        public static Sprite FromImage(string imagePath)
        {
            return new SimpleSprite(imagePath);
        }

        public static Sprite FromTexture(Texture2D texture)
        {
            var sprite = new BlankSprite(texture.Name)
            {
                Texture = texture,
                Width = texture.Width,
                Height = texture.Height
            };

            return sprite;
        }

        public class BlankSprite : Sprite
        {
            private readonly string _name;

            public BlankSprite(string name)
            {
                _name = name;
            }

            public override string Name
            {
                get { return _name; }
            }

            protected override void BeforeDraw()
            {
            }

            protected override void OnLoad()
            {
            }

            protected override void OnUnload()
            {
            }

            protected override void OnDispose()
            {
            }

            protected override void OnDisplay()
            {
            }
        }

        public class SimpleSprite : Sprite
        {
            private string _imagePath;
            public string ImagePath
            {
                get
                {
                    return _imagePath;
                }
                set
                {
                    _imagePath = value;
                    Texture = GhostClient.Ghost.CurrentGhostGame.Content.Load<Texture2D>(_imagePath);

                    Width = Texture.Width;
                    Height = Texture.Height;
                }
            }

            public SimpleSprite(string imagePath)
            {
                ImagePath = imagePath;
            }

            public override string Name
            {
                get { return "simple_sprite@" + _imagePath; }
            }

            protected override void BeforeDraw()
            {
            }

            protected override void OnLoad()
            {
            }

            protected override void OnUnload()
            {
            }

            protected override void OnDispose()
            {
            }

            protected override void OnDisplay()
            {
            }
        }
    }
}
