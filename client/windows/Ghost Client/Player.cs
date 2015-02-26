using Sharp2D;

namespace Ghost
{
    public abstract class Player : PhysicsSprite
    {
        public float XVel { get; protected set; }

        public float YVel { get; protected set; }

        public int PlayerNumber { get; private set; }

        public bool IsVisible
        {
            get { return Alpha == 1f; }
            set
            {
                
            }
        }

        public override string Name
        {
            get { return "Player" + PlayerNumber; }
        }

        protected Player(int playerNumber)
        {
            this.PlayerNumber = playerNumber;
        }

        protected override void BeforeDraw()
        {
        }

        protected override void OnDisplay()
        {
        }

        public override void Update()
        {
            base.Update();

            OnInput();

            OnMovement();
        }

        protected abstract void OnInput();

        protected virtual void OnMovement()
        {
            X += XVel;
            Y += YVel;
        }
    }
}
