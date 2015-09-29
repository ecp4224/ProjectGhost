using Microsoft.Xna.Framework.Graphics;

namespace Ghost.Sprites.Items
{
    public class FireRateItem : Entity
    {
        public FireRateItem(short id) : base(id)
        {
        }

        protected override void OnLoad()
        {
            Texture = GhostClient.Ghost.CurrentGhostGame.Content.Load<Texture2D>("sprites/party_cannon.png");

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
