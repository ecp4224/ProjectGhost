using System;
using System.Collections.Generic;
using Ghost.Core.Sharp2D_API;
using Ghost.Sprites.Map;
using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Graphics;

namespace Ghost.Sprites
{
    public class Mirror : Entity, IPhysics
    {
        public override float X
        {
            get { return base.X; }
            set
            {
                if (this.Hitbox != null)
                {
                    float diff = base.X - value;
                    this.Hitbox.Polygon.Translate(diff, 0);
                }
                base.X = value;
            }
        }

        public override float Y
        {
            get { return base.Y; }
            set
            {
                if (this.Hitbox != null)
                {
                    float diff = base.Y - value;
                    this.Hitbox.Polygon.Translate(0, diff);
                }
                base.Y = value;
            }
        }

        public Hitbox Hitbox { get; private set; }
        public Mirror(short id) : base(id)
        {
        }

        protected override void OnLoad()
        {
            Texture = GhostClient.Ghost.CurrentGhostGame.Content.Load<Texture2D>("sprites/wall.png");

            Width = Texture.Width;
            Height = Texture.Height;

            PhysicsObjects.PhysicsItems.Add(this);

            float x1 = X - (Width/2f), x2 = X + (Width/2f);
            float y1 = Y - (Height/2f), y2 = Y + (Height/2f);

            var points = new[]
            {
                new Vector2(x1, y1),
                new Vector2(x1, y2),
                new Vector2(x2, y2),
                new Vector2(x2, y1),
            };

            points = Vector2Utils.rotatePoints(Rotation, Position, points);

            this.Hitbox = new Hitbox("MIRROR", points);
        }

        protected override void OnUnload()
        {
            PhysicsObjects.PhysicsItems.Remove(this);
        }

        protected override void OnDispose()
        {

        }
    }
}
