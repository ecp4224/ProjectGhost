using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Graphics;
using Sharp2D;
using Sharp2D.Core.Interfaces;

namespace GhostClient.Core
{
    public class TextSprite : Sprite
    {
        public SpriteFont Font { get; private set; }
        public string Text { get; set; }

        public static TextSprite CreateText(string text, string font)
        {
            return new TextSprite
            {
                Text = text,
                Font = Ghost.CurrentGhostGame.Content.Load<SpriteFont>(font)
            };
        }
        
        private TextSprite()
        {
        }

        public override void DrawColor(SpriteBatch batch)
        {
            batch.DrawString(Font, Text, Position, Color, Rotation, Origin, Scale, SpriteEffects.None, Layer);
        }

        public override string Name
        {
            get { return Text + " _ " + Font.Texture.Name; }
        }

        protected override void BeforeDraw()
        {
        }

        protected override void OnLoad()
        {
            this.Texture = Font.Texture; //Prevent error
            var size = Font.MeasureString(Text);
            Width = size.X;
            Height = size.Y;
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
