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

        public void Animate()
        {
            AnimationHelper.CreateDynamicAnimation(delegate(long l)
            {
                TexCoords = new Rectangle(0, 0, (int)MathUtils.Ease(0f, 1040f, 400f, l), 64);
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
        }

        protected override void OnUnload()
        {
        }

        protected override void OnDispose()
        {
        }
    }
}
