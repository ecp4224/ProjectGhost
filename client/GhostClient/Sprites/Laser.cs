using Ghost.Core;
using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Graphics;
using Sharp2D;

namespace Ghost.Sprites
{
    public class Laser : Entity
    {
        public Laser(short id) : base(id)
        {
        }

        private bool _animated;
        public void Animate()
        {
            if (_animated)
                return;

            _animated = true;
            AnimationHelper.CreateDynamicAnimation(delegate(long l)
            {
                TexCoords = new Rectangle(0, 0, (int)MathUtils.Ease(0f, 1040f, 300f, l), 64);
            }).Until(() => TexCoords.HasValue && TexCoords.Value.Width > 1040).OnEnded(delegate
            {
                TexCoords = new Rectangle(0, 0, 1050, 64);
            }).Start();
        }

        protected override void OnLoad()
        {
            Texture = GhostClient.Ghost.CurrentGhostGame.Content.Load<Texture2D>("sprites/laser.png");

            Width = Texture.Width;
            Height = Texture.Height;

            NeverClip = true;

            Origin = new Vector2(0, 32);
        }

        protected override void OnDisplay()
        {
            base.OnDisplay();

            Origin = new Vector2(0, 32);
        }

        protected override void OnUnload()
        {
        }

        protected override void OnDispose()
        {
        }
    }
}
