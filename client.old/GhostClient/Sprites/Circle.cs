using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Ghost.Core;
using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Graphics;
using Sharp2D;

namespace Ghost.Sprites
{
    public class Circle : Entity
    {
        public Circle(short id) : base(id)
        {
        }

        public override float Alpha
        {
            get { return !_animated ? 0.7f : _alpha; }
            set
            {
                if (!_animated && value > 0f)
                {
                    Animate();
                }
                else if (_animated)
                {
                    _alpha = value;
                }
            }
        }

        private bool _animated;
        private float _alpha = 1f;

        private void Animate()
        {
            if (_animated)
                return;

            _animated = true;

            base.Texture = GhostClient.Ghost.CurrentGhostGame.Content.Load<Texture2D>("sprites/circle_filled.png");
        }

        protected override void OnLoad()
        {
            Texture = GhostClient.Ghost.CurrentGhostGame.Content.Load<Texture2D>("sprites/circle.png");

            Width = Texture.Width;
            Height = Texture.Height;

            NeverClip = true;

            Scale = new Vector2(0.0001f);
        }

        protected override void OnDisplay()
        {
            base.OnDisplay();

            AnimationHelper.CreateDynamicAnimation(delegate(long l)
            {
                Scale = new Vector2(MathUtils.Ease(0.0001f, 1f, 550, l));
            }).Until(() => Scale.X == 1f).Start();
        }

        protected override void OnUnload()
        {
        }

        protected override void OnDispose()
        {
        }
    }
}
