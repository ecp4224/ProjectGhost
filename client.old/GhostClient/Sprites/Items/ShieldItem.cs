using Microsoft.Xna.Framework.Graphics;

namespace Ghost.Sprites.Items
{
    public class ShieldItem : Entity
    {
        public ShieldItem(short id) : base(id)
        {
        }

        protected override void OnLoad()
        {
            Texture = GhostClient.Ghost.CurrentGhostGame.Content.Load<Texture2D>("sprites/can_of_scummy.png");

            Width = Texture.Width;
            Height = Texture.Height;
        }

        protected override void OnUnload()
        {

        }

        protected override void OnDispose()
        {

        }
    }
}