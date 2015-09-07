using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Ghost.Sprites.Items;
using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Graphics;
using Sharp2D;
using Sharp2D.Core.Interfaces;

namespace Ghost.Sprites.Effects
{
    public class CircleEffect : IEffect
    {
        private static Random random = new Random();
        public void Begin(int duration, int size, float x, float y, double rotation)
        {
            int stage1Duration = (int) rotation; //The rotation field is used as the first stage duration
            int stage2Duration = duration - stage1Duration;

            var emittor = new CircleEmittor(stage1Duration, stage2Duration, x, y, size);
            GhostClient.Ghost.CurrentGhostGame.AddLogical(emittor);
        }

        private class CircleEmittor : ILogical
        {
            private const long DURATION = 100;

            private long lastSpawn, start;
            public int radius, stage1Duration, stage2Duration;
            public double startPos;
            public float x, y;

            public bool stage2;
            private int totalSpawn;
            private int cursor = 0;

            public CircleEmittor(int stage1, int stage2, float x, float y, int size)
            {
                this.stage1Duration = stage1;
                this.stage2Duration = stage2;
                this.x = x;
                this.y = y;
                this.startPos = random.Next();
                this.radius = size;
            }

            public void Dispose()
            {
                
            }

            public void Update()
            {
                if (start == 0)
                {
                    start = Environment.TickCount;
                }
                if (Environment.TickCount - lastSpawn < DURATION)
                    return;

                int spawnCount;
                if (stage2)
                {
                    spawnCount = random.Next(10, 30);
                    spawnCount = Math.Min(totalSpawn - cursor, spawnCount);
                    cursor += spawnCount;
                }
                else
                {
                    spawnCount = random.Next(-1, 9);
                }

                for (int i = 0; i < spawnCount; i++)
                {
                    if (!stage2)
                    {
                        var p = new CircleParticle(this) {X = x, Y = y};
                        GhostClient.Ghost.CurrentGhostGame.AddSprite(p);
                    }
                    else
                    {
                        int xd = random.Next();

                        float tempx = (float) (x + (Math.Cos(xd)*radius));
                        float tempy = (float) (y + (Math.Sin(xd)*radius));

                        var p = new CircleParticle(this) {X = tempx, Y = tempy};
                        GhostClient.Ghost.CurrentGhostGame.AddSprite(p);
                    }
                }

                lastSpawn = Environment.TickCount;

                if (!stage2 && Environment.TickCount - start >= stage1Duration)
                {
                    stage2 = true;
                    start = Environment.TickCount;
                    totalSpawn = random.Next(300, 400);
                }
                else if (stage2 && Environment.TickCount - start >= stage2Duration)
                {
                    GhostClient.Ghost.CurrentGhostGame.RemoveLogical(this);
                }
            }
        }

        private class CircleParticle : Entity
        {
            private double SPEED;

            private bool stage2;
            private CircleEmittor emittor;
            private double startPos, counter;

            private Vector2 target = Vector2.Zero;
            private int duration;
            private int start;
            private float sx, sy;
            public CircleParticle(CircleEmittor emittor) : base(0)
            {
                stage2 = emittor.stage2;
                this.emittor = emittor;
                SPEED = (2.0*Math.PI)/(random.Next(emittor.stage1Duration / 2, emittor.stage1Duration)/16.0);
                startPos = emittor.startPos;
                this.BlendMode = BlendState.Additive;
            }

            protected override void OnLoad()
            {
                Texture = GhostClient.Ghost.CurrentGhostGame.Content.Load<Texture2D>("sprites/ball.png");

                Width = Texture.Width;
                Height = Texture.Height;

                NeverClip = true;
                UniformScale = (float)(random.NextDouble() * (0.3f - 0.15f) + 0.15f);
                TintColor = System.Drawing.Color.FromArgb(255, 36, 81, 163);
            }

            protected override void OnUnload()
            {
                emittor = null;
            }

            protected override void OnDispose()
            {
            }

            public override void Update()
            {
                base.Update();

                if (!stage2 && emittor.stage2)
                {
                    stage2 = true;
                }

                if (stage2)
                {
                    TintColor = System.Drawing.Color.FromArgb(255, 170, 19, 27);
                    if (target == Vector2.Zero)
                    {
                        double tx = emittor.x - X;
                        double ty = emittor.y - Y;
                        double angle = Math.Atan2(ty, tx);
                        int dis = random.Next(300, 500);

                        target = new Vector2(X + (dis * (float)Math.Cos(angle)), Y + (dis * (float)Math.Sin(angle)));
                        duration = random.Next(50, 700);
                        start = Environment.TickCount;
                        sx = X;
                        sy = Y;
                    }

                    X = MathUtils.Ease(sx, target.X, duration, Environment.TickCount - start);
                    Y = MathUtils.Ease(sy, target.Y, duration, Environment.TickCount - start);
                    Alpha = MathUtils.Ease(1, 0, duration / 1.5f, Environment.TickCount - start);

                    if (X == target.X && Y == target.Y)
                    {
                        GhostClient.Ghost.CurrentGhostGame.RemoveSprite(this);
                    }
                }
                else
                {
                    counter += SPEED;

                    X = (float) (emittor.x + Math.Cos(startPos + counter)*emittor.radius);
                    Y = (float) (emittor.y + Math.Sin(startPos + counter)*emittor.radius);
                }
            }
        }
    }
}
