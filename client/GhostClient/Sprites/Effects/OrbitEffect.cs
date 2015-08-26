using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Xna.Framework.Graphics;
using Sharp2D;
using Sharp2D.Core.Interfaces;

namespace Ghost.Sprites.Effects
{
    public class OrbitEffect : ILogical
    {
        private NetworkPlayer owner;
        private double startPos;
        private double count;
        private const double SPEED = (2.0*Math.PI)/65; //Take 1.5 seconds to do full circle
        private static readonly Random Random = new Random();

        public OrbitEffect(NetworkPlayer owner)
        {
            this.owner = owner;
        }

        public void Begin()
        {
            startPos = Random.NextDouble()*(2.0 * Math.PI);

            GhostClient.Ghost.CurrentGhostGame.AddLogical(this);

            owner.Orbits.Add(this);
        }

        public void End()
        {
            GhostClient.Ghost.CurrentGhostGame.RemoveLogical(this);

            owner.Orbits.Remove(this);
        }

        public void Dispose()
        {
        }

        public void Update()
        {
            count += SPEED;

            if (Math.Abs(owner.Alpha) < 0.05)
                return; //Don't spawn particles when you can't see them!

            double x = Math.Cos(count + startPos) * (owner.Width / 2.0);
            double y = Math.Sin(count + startPos) * (owner.Height / 2.0);

            x += owner.X;
            y += owner.Y;

            int spawnCount = Random.Next(0, 8);

            for (int i = 0; i < spawnCount; i++)
            {
                var sprite = new OrbitSprite(count + startPos - 1.57079633);
                sprite.X = (float) x;
                sprite.Y = (float) y;
                sprite.Alpha = owner.Alpha;
                owner.CurrentWorld.AddSprite(sprite);
                //owner.Attach(sprite);
            }
        }

        public class OrbitSprite : Entity
        {
            private double direction;
            private float speed;
            private int duration;
            private float startAlpha;
            private int start;
            public OrbitSprite(double baseDirection)
                : base(0)
            {
                this.direction = baseDirection + (Random.NextDouble()*0.34906585D); // +- 20 degrees
                this.speed = (float) Random.NextDouble();
                this.duration = Random.Next(300, 1100);
                BlendMode = BlendState.Additive;

                XVel = (float) (Math.Cos(direction)*speed);
                YVel = (float) (Math.Sin(direction)*speed);
            }

            protected override void OnLoad()
            {
                Texture = GhostClient.Ghost.CurrentGhostGame.Content.Load<Texture2D>("sprites/ball.png");

                Width = Texture.Width;
                Height = Texture.Height;

                NeverClip = true;
                UniformScale = (float)(Random.NextDouble() * (0.13f - 0.05f) + 0.05f);
                TintColor = System.Drawing.Color.FromArgb(255, 222, 248, 9);
                startAlpha = Alpha;
            }

            protected override void OnUnload()
            {
            }

            protected override void OnDispose()
            {
            }

            protected override void OnDisplay()
            {
                base.OnDisplay();

                start = Environment.TickCount;
            }

            public override void Update()
            {
                base.Update();

                Alpha = MathUtils.Ease(startAlpha, 0f, duration, Environment.TickCount - start);

                if (Alpha == 0f)
                {
                    GhostClient.Ghost.CurrentGhostGame.RemoveSprite(this);
                }
            }
        }
    }
}
