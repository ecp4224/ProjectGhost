using System;
using System.Collections.Generic;
using Ghost.Core.Sharp2D_API;
using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Graphics;

namespace Ghost.Sprites
{
    public class Mirror : Entity
    {
        public static List<Mirror> MIRRORS = new List<Mirror>(); 

        private static readonly Random rand = new Random();

        public Hitbox Hitbox { get; private set; }
        public Mirror(short id) : base(id)
        {
        }

        protected override void OnLoad()
        {
            Texture = GhostClient.Ghost.CurrentGhostGame.Content.Load<Texture2D>("sprites/wall.png");

            Width = Texture.Width;
            Height = Texture.Height;

            TintColor = System.Drawing.Color.FromArgb(rand.Next(255), rand.Next(255), rand.Next(255));

            MIRRORS.Add(this);

            float x1 = X - (Width/2f), x2 = X + (Width/2f);
            float y1 = Y - (Height/2f), y2 = Y + (Height/2f);

            this.Hitbox = new Hitbox("MIRROR", new[]
            {
                new Vector2(x1, y1),
                new Vector2(x1, y2),
                new Vector2(x2, y2),
                new Vector2(x2, y1), 
            });
            this.Hitbox.Polygon.Rotate(Rotation);
        }

        protected override void OnUnload()
        {
            MIRRORS.Remove(this);
        }

        protected override void OnDispose()
        {

        }
    }
}
