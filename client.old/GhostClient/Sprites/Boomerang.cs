using Microsoft.Xna.Framework.Graphics;

namespace Ghost
{
    public class Boomerang : NetworkPlayer
    {
        public Boomerang(short id) : base(id, "BOOMERANG")
        {
        }

        protected override void OnLoad()
        {
            base.OnLoad();
            Texture = GhostClient.Ghost.CurrentGhostGame.Content.Load<Texture2D>("sprites/boomerang.png");
            UniformScale = 0.75f;
        }

        protected override void UpdateLifeBalls()
        {
            //do nothing
        }
    }
}
