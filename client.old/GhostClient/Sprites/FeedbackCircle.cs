using System;
using System.Collections.Generic;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using Ghost.Core;
using Microsoft.Xna.Framework.Graphics;
using Sharp2D;

namespace Ghost
{
    public class FeedbackCircle : Sprite
    {
        public override string Name
        {
            get { return "feedback"; }
        }

        protected override void BeforeDraw()
        {
        }

        protected override void OnLoad()
        {
            Texture = GhostClient.Ghost.CurrentGhostGame.Content.Load<Texture2D>("sprites/ball.png");

            Width = Texture.Width;
            Height = Texture.Height;

            UniformScale = 0.75f;

            NeverClip = true;

            Scale = Scale/2f;

            TintColor = System.Drawing.Color.FromArgb(255, 72, 170, 45);
        }

        protected override void OnUnload()
        {
        }

        protected override void OnDispose()
        {
        }

        protected override void OnDisplay()
        {
            float start = UniformScale;
            int startTime = Environment.TickCount;

            var sprite = Sprite.FromImage("sprites/ball.png");
            sprite.UniformScale = UniformScale - 0.05f;
            sprite.TintColor = System.Drawing.Color.Black;
            sprite.X = X;
            sprite.Y = Y;
            sprite.NeverClip = true;
            Attach(sprite);
            CurrentWorld.AddSprite(sprite);

            AnimationHelper.CreateDynamicAnimation(delegate()
            {
                float temp = MathUtils.Ease(start, 0.0001f, 350, Environment.TickCount - startTime);
                UniformScale = temp;

                foreach (Sprite s in Children.OfType<Sprite>())
                {
                    s.UniformScale = temp - 0.05f;
                }
            })
                .Until(() => UniformScale == 0.0001f)
                .OnEnded(() =>
                {
                    foreach (Sprite s in Children.OfType<Sprite>())
                    {
                        CurrentWorld.RemoveSprite(s);
                    }
                    CurrentWorld.RemoveSprite(this);
                })
                .Start();
        }
    }
}
