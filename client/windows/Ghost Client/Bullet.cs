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
            TintColor = Players.PlayerColors[3];
        }
    }
}
