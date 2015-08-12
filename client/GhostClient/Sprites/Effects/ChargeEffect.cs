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
            int lastSpawn = 0;
            int nextSpawn = 0;
            ILogical[] temp = {null};
            int startTime = Environment.TickCount;

            temp[0] = GhostClient.Ghost.CurrentGhostGame.AddLogical(delegate
            {
                if (Environment.TickCount - startTime >= duration)
                {
                    GhostClient.Ghost.CurrentGhostGame.RemoveLogical(temp[0]);
                    return;
                }
                if (Environment.TickCount - lastSpawn > nextSpawn)
                {
                    int count = random.Next(1, 4);
                    for (int i = 0; i < count; i++)
                    {
                        double circleLocation = random.NextDouble()* (2.0 * Math.PI);

                        float spawnX = (float) ((Math.Cos(circleLocation)*size) + x);
                        float spawnY = (float) ((Math.Sin(circleLocation)*size) + y);

                        ChargeSprite sprite = new ChargeSprite(7789, x, y);
                        sprite.X = spawnX;
                        sprite.Y = spawnY;
                        sprite.Rotation = (float) rotation;

                        ChargeSprite sprite2 = new ChargeSprite(7789, x, y, 2);
                        sprite2.X = spawnX;
                        sprite2.Y = spawnY;
                        sprite2.Rotation = (float)rotation;
                        sprite.Attach(sprite2);

                        GhostClient.Ghost.CurrentGhostGame.AddSprite(sprite);
                        GhostClient.Ghost.CurrentGhostGame.AddSprite(sprite2);

                    }

                    lastSpawn = Environment.TickCount;
                    nextSpawn = random.Next(10, 100);
                }
            });
        }

        public class ChargeSprite : Entity
        {
            private float centerX, centerY, duration, startX, startY;
            private int startTime, spriteNum;
            public ChargeSprite(short id, float centerX, float centerY, int spriteNum = 1) : base(id)
            {
                this.centerX = centerX;
                this.centerY = centerY;
                this.duration = random.Next(100, 1000);
                this.spriteNum = spriteNum;
            }

            protected override void OnLoad()
            {
                Texture = GhostClient.Ghost.CurrentGhostGame.Content.Load<Texture2D>("sprites/ball.png");

                Width = Texture.Width;
                Height = Texture.Height;

                NeverClip = true;

                if (spriteNum == 1)
                {
                    UniformScale = 0.18F;
                    TintColor = System.Drawing.Color.FromArgb(255, 25, 158, 208);
                }
                else
                {
                    UniformScale = 0.25F;
                    TintColor = System.Drawing.Color.FromArgb(255, 24, 205, 235);
                    Alpha = 0.3f;
                }
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

                if (spriteNum != 1)
                    return;

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
