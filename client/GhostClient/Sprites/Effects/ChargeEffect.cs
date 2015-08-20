using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Xna.Framework.Graphics;
using Sharp2D;
using Sharp2D.Core.Interfaces;

namespace Ghost.Sprites.Effects
{
    public class ChargeEffect : IEffect
    {
        private static Random random = new Random();
        public void Begin(int duration, int size, float x, float y, double rotation)
        {
            int count = random.Next(100, 200);
            ChargeSprite[] sprites = new ChargeSprite[count];

            for (int i = 0; i < count; i++)
            {
                double circleLocation = random.NextDouble() * (2.0 * Math.PI);
                int trueSize = random.Next(size - (size/2)) + size;

                float spawnX = (float)((Math.Cos(circleLocation) * trueSize) + x);
                float spawnY = (float)((Math.Sin(circleLocation) * trueSize) + y);

                var sprite = new ChargeSprite(7789, x, y) {X = spawnX, Y = spawnY, Rotation = (float) rotation};

                sprites[i] = sprite;
            }
            int cursor = 0;

            int lastSpawn = 0;
            int nextSpawn = 0;
            ILogical[] temp = {null};
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
                    int toSpawn = random.Next(20, 40);
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

        public class ChargeSprite : Entity
        {
            private float centerX, centerY, duration, startX, startY;
            private int startTime;
            public ChargeSprite(short id, float centerX, float centerY) : base(id)
            {
                this.centerX = centerX;
                this.centerY = centerY;
                this.duration = random.Next(100, 1000);
                this.BlendMode = BlendState.Additive;
            }

            protected override void OnLoad()
            {
                Texture = GhostClient.Ghost.CurrentGhostGame.Content.Load<Texture2D>("sprites/ball.png");

                Width = Texture.Width;
                Height = Texture.Height;

                NeverClip = true;
                UniformScale = (float) (random.NextDouble()*(0.2f - 0.13f) + 0.13f);
                TintColor = System.Drawing.Color.FromArgb(255, 25, 158, 208);
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
                startX = X;
                startY = Y;
            }

            public override void Update()
            {
                base.Update();

                float newX = MathUtils.Ease(startX, centerX, duration, (Environment.TickCount - startTime));
                float newY = MathUtils.Ease(startY, centerY, duration, (Environment.TickCount - startTime));
                float newAlpha = MathUtils.Ease(1f, 0f, duration, (Environment.TickCount - startTime));

                X = newX;
                Y = newY;
                Alpha = newAlpha;

                if (Alpha == 0f)
                {
                    GhostClient.Ghost.CurrentGhostGame.RemoveSprite(this);
                }
            }
        }
    }
}
