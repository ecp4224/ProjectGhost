using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Xna.Framework.Graphics;
using Sharp2D;
using Sharp2D.Core.Interfaces;

namespace Ghost.Sprites.Effects
{
    public class LineEffect : IEffect
    {
        private static Random random = new Random();
        public void Begin(int duration, int size, float x, float y, double rotation)
        {
            int count = random.Next(400, 600);
            LineSprite[] sprites = new LineSprite[count];

            for (int i = 0; i < count; i++)
            {
                double range = (size/1000.0) - 0.01;
                double angleToAdd = random.NextDouble()* (range - -range) + -range;

                var sprite = new LineSprite(7789, rotation + angleToAdd, duration) { X = x, Y = y, Rotation = (float)(rotation + angleToAdd) };

                sprites[i] = sprite;
            }
            int cursor = 0;

            int lastSpawn = 0;
            int nextSpawn = 0;
            ILogical[] temp = { null };
            int startTime = Environment.TickCount;

            temp[0] = GhostClient.Ghost.CurrentGhostGame.AddLogical(delegate
            {
                if (Environment.TickCount - startTime >= duration)
                {
                    GhostClient.Ghost.CurrentGhostGame.RemoveLogical(temp[0]);
                    sprites = null;
                    return;
                }
                if (Environment.TickCount - lastSpawn > nextSpawn)
                {
                    int toSpawn = random.Next(40, 80);
                    toSpawn = Math.Min(count - cursor, toSpawn);
                    for (int i = cursor; i < cursor + toSpawn; i++)
                    {
                        GhostClient.Ghost.CurrentGhostGame.AddSprite(sprites[i]);
                    }
                    cursor += toSpawn;
                    lastSpawn = Environment.TickCount;
                    nextSpawn = random.Next(10, 100);
                }
            });
        }

        public sealed class LineSprite : Entity
        {
            private int duration;
            private double directoin;
            private int startTime;
            public LineSprite(short id, double rotation, int baseDuration)
                : base(id)
            {
                this.directoin = rotation;
                this.duration = random.Next(baseDuration, (int) (baseDuration*1.5));
                int speed = random.Next(30, 80);
                XVel = (float) (Math.Cos(directoin)*speed);
                YVel = (float) (Math.Sin(directoin)*speed);
                TargetX = 9999f;
                TargetY = 9999f;
                BlendMode = BlendState.Additive;
            }

            protected override void OnLoad()
            {
                Texture = GhostClient.Ghost.CurrentGhostGame.Content.Load<Texture2D>("sprites/ball.png");

                Width = Texture.Width;
                Height = Texture.Height;

                NeverClip = true;
                UniformScale = (float)(random.NextDouble() * (0.35f - 0.2f) + 0.2f);
                TintColor = System.Drawing.Color.FromArgb(255, 194, 19, 19);
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
                startTime = Environment.TickCount;
            }

            public override void Update()
            {
                base.Update();
                float newAlpha = MathUtils.Ease(1f, 0f, duration, (Environment.TickCount - startTime));

                Alpha = newAlpha;

                if (Alpha == 0f)
                {
                    GhostClient.Ghost.CurrentGhostGame.RemoveSprite(this);
                }
            }
        }
    }
}
