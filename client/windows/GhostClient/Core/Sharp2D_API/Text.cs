using System;
using System.Collections.Generic;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using GhostClient.Core.Sharp2D_API;
using Microsoft.Xna.Framework.Graphics;

namespace Sharp2D
{
    public static class Text
    {
        private static readonly Dictionary<string, SizeF> Sizes = new Dictionary<string, SizeF>();
        private static readonly Dictionary<string, Texture2D> TextureCache = new Dictionary<string, Texture2D>(); 
        public static TextSprite CreateTextSprite(string text)
        {
            return CreateTextSprite(text, Color.Black, SystemFonts.DefaultFont);
        }

        public static TextSprite CreateTextSprite(string text, Color color)
        {
            return CreateTextSprite(text, color, SystemFonts.DefaultFont);
        }

        public static TextSprite CreateTextSprite(string text, Color color, Font font)
        {
            Texture2D texture;
            string name = "TEXT_" + text + "_FONT_" + font.Name;

            if (!TextureCache.ContainsKey(name))
            {
                var bitmap = new Bitmap((int) ((font.Size*text.Length) + 64), (int) (font.GetHeight() + 64));

                SizeF stringSize;
                using (var graphics = Graphics.FromImage(bitmap))
                {
                    using (var brush = new SolidBrush(color))
                    {
                        graphics.DrawString(text, font, brush, 0f, 0f);
                    }

                    stringSize = graphics.MeasureString(text, font);
                }

                texture = Texture2DExt.FromBitmap(bitmap);
                texture.Name = "TEXT_" + text + "_FONT_" + font.Name;

                Sizes.Add(texture.Name, stringSize);
                TextureCache.Add(name, texture);
            }
            else
            {
                texture = TextureCache[name];
            }

            return new TextSprite(texture.Name, Sizes[texture.Name], text)
            {
                Texture = texture,
                Width = texture.Width,
                Height = texture.Height
            };
        }
    }

    public sealed class TextSprite : Sprite.BlankSprite
    {
        public SizeF StringSize { get; private set; }

        public string Text { get; private set; }

        public float StringWidth
        {
            get { return StringSize.Width; }
        }

        public float StringHeight
        {
            get { return StringSize.Height; }
        }

        public TextSprite(string name, SizeF stringSize, string text)
            : base(name)
        {
            StringSize = stringSize;
        }
    }
}
