using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Ghost.Core.Sharp2D_API;
using Ghost.Sprites.Map;
using Microsoft.Xna.Framework;
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

            private bool didHit;
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
                    return;
                }

                CheckPhysics();
            }

            private void CheckPhysics()
            {
                foreach (IPhysics item in PhysicsObjects.PhysicsItems)
                {
                    Hitbox hitbox = item.Hitbox;

                    if (!Vector2Utils.isPointInside(Position, hitbox.Polygon.Points) && !WillIntersect(hitbox))
                    {
                        continue;
                    }

                    if (hitbox.Name == "WALL")
                    {
                        GhostClient.Ghost.CurrentGhostGame.RemoveSprite(this);
                        return;
                    }

                    Vector2 oldPoint = new Vector2(X - (XVel * 1.5f), Y - (YVel * 1.5f));
                    Vector2 endPoint = new Vector2(X + (XVel * 100), Y + (YVel * 100));

                    Face closestFace = null;
                    Vector2 closestPoint = Vector2.Zero;
                    double distance = 9999999999.0;
                    foreach (Face face in hitbox.Polygon.Faces)
                    {
                        Vector2 pointOfIntersection = Vector2Utils.pointOfIntersection(oldPoint, endPoint, face.PointA,
                            face.PointB);
                        if (pointOfIntersection == Vector2.Zero)
                            continue;

                        double d = Vector2.Distance(pointOfIntersection, oldPoint);
                        if (closestFace == null)
                        {
                            closestFace = face;
                            closestPoint = pointOfIntersection;
                            distance = d;
                        }
                        else if (d < distance)
                        {
                            closestFace = face;
                            closestPoint = pointOfIntersection;
                            distance = d;
                        }
                    }

                    if (closestFace == null)
                        return;

                    if (didHit)
                    {
                        GhostClient.Ghost.CurrentGhostGame.RemoveSprite(this);
                        return;
                    }

                    Vector2 normal = closestFace.Normal;
                    float p = Vector2.Dot(new Vector2(XVel, YVel), normal)*-2f;
                    Vector2 newVel = new Vector2(normal.X, normal.Y);
                    newVel *= p;
                    newVel.X += XVel;
                    newVel.Y += YVel;

                    XVel = newVel.X;
                    YVel = newVel.Y;

                    X = closestPoint.X;
                    Y = closestPoint.Y;

                    didHit = true;
                }
            }

            private bool WillIntersect(Hitbox hitbox)
            {
                Vector2 startPoint = new Vector2(X, Y);
                Vector2 endPoint = new Vector2(X + XVel, Y + YVel);
                int numOfIntersections = 0;

                foreach (Face face in hitbox.Polygon.Faces)
                {
                    Vector2 intersect = Vector2Utils.pointOfIntersection(startPoint, endPoint, face.PointA, face.PointB);
                    if (intersect == Vector2.Zero)
                        continue;
                    numOfIntersections++;
                }

                return numOfIntersections != 0 && numOfIntersections%2 == 0;
            }
        }
    }
}
