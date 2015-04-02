using System.Drawing;

namespace Ghost
{
    public class Bullet : NetworkPlayer
    {
        public Bullet(short id, string name) : base(id, name)
        {
        }

        protected override void OnLoad()
        {
            base.OnLoad();

            Scale = 0.25f;
            TintColor = Color.FromArgb(255, 29, 53, 214);
        }

        protected override void UpdateLifeBalls()
        {
            //do nothing
        }
    }
}
