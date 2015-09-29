using Microsoft.Xna.Framework.Graphics;

namespace Ghost.Sprites.Items
{
    public class EmpItem : Entity
    {
        public EmpItem(short id) : base(id)
        {
        }

        protected override void OnLoad()
        {
            Texture = GhostClient.Ghost.CurrentGhostGame.Content.Load<Texture2D>("sprites/can_of_chummy.png");

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
