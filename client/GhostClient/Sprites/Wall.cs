using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Xna.Framework.Graphics;

namespace Ghost.Sprites
{
    public class Wall : Entity
    {
        public Wall(short id) : base(id)
        {
        }

        protected override void OnLoad()
        {
            Texture = GhostClient.Ghost.CurrentGhostGame.Content.Load<Texture2D>("sprites/wall.png");

            Width = Texture.Width;
            Height = Texture.Height;
        }

        protected override void OnUnload()
        {

        }

        protected override void OnDispose()
        {

        }
    }
}
