using System;
using System.Collections.Generic;
using System.Drawing;
using System.Linq;
using System.Text;
using Microsoft.Xna.Framework.Graphics;

namespace Ghost.Sprites
{
    public class Mirror : Entity
    {
        private static readonly Random rand = new Random();
        public Mirror(short id) : base(id)
        {
        }

        protected override void OnLoad()
        {
            Texture = GhostClient.Ghost.CurrentGhostGame.Content.Load<Texture2D>("sprites/wall.png");

            Width = Texture.Width;
            Height = Texture.Height;

            TintColor = System.Drawing.Color.FromArgb(rand.Next(255), rand.Next(255), rand.Next(255));
        }

        protected override void OnUnload()
        {

        }

        protected override void OnDispose()
        {

        }
    }
}
