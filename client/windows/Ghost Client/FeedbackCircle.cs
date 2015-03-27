using System;
using System.Collections.Generic;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Ghost.Core;
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
            Texture = Texture.NewTexture("sprites/ball.png");
            if (!Texture.Loaded)
                Texture.LoadTextureFromFile();

            Width = Texture.TextureWidth;
            Height = Texture.TextureHeight;

            Scale = 0.75f;

            NeverClip = true;

            Scale = Scale/2f;

            TintColor = Color.FromArgb(255, 72, 170, 45);
        }

        protected override void OnUnload()
        {
        }

        protected override void OnDispose()
        {
        }

        protected override void OnDisplay()
        {
            var sprite = Sprite.FromImage("sprites/ball.png");
            sprite.Scale = Scale - 0.05f;
            sprite.TintColor = Color.Black;
            sprite.X = X;
            sprite.Y = Y;
            sprite.NeverClip = true;
            Attach(sprite);
            CurrentWorld.AddSprite(sprite);


            float start = Scale;
            float startTime = Environment.TickCount;

            AnimationHelper.CreateDynamicAnimation(delegate()
            {
                float temp = MathUtils.Ease(start, 0.0001f, 500, Environment.TickCount - startTime);
                Scale = temp;
                foreach (Sprite s in Children.OfType<Sprite>())
                {
                    s.Scale = temp - 0.05f;
                }
            })
                .Until(() => Scale == 0.0001f)
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
