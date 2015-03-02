using Ghost.Core;
using Sharp2D;

namespace Ghost
{
    public abstract class Player : PhysicsSprite
    {
        private float _xVel, _yVel;
        public virtual float XVel
        {
            get { return _xVel; }
            set
            {
                if (_frozen) return;
                _xVel = value; 
            }
        }

        public virtual float YVel
        {
            get { return _yVel; }
            set
            {
                if (_frozen) return;
                _yVel = value; 
            }
        }

        private bool _frozen;

        public bool Frozen
        {
            get { return _frozen; }
            set
            {
                _frozen = value;
                XVel = 0;
                YVel = 0;
            }
        }

        public int PlayerNumber { get; private set; }

        private DynamicAnimation _currentAnimation;
        public new bool IsVisible
        {
            get { return Alpha > 0f; }
            set
            {
                if (!value)
                {
                    if (Alpha == 0f)
                        return;
                    if (_currentAnimation != null && !_currentAnimation.HasEnded)
                        return;
                    _currentAnimation = AnimationHelper.CreateDynamicAnimation(delegate()
                    {
                        Alpha = MathUtils.Ease(1f, 0f, 800, _currentAnimation.Elaspe);
                    }).Until(() => Alpha == 0f)
                        .OnEnded(() => Alpha = 0f)
                        .Start();
                }
                else
                {
                    if (_currentAnimation != null)
                    {
                        if (!_currentAnimation.HasEnded)
                        {
                            _currentAnimation.End();
                        }
                        _currentAnimation = null;
                    }
                    Alpha = 1f;
                }
            }
        }

        public override string Name
        {
            get { return "Player" + PlayerNumber; }
        }

        protected Player(int playerNumber)
        {
            PlayerNumber = playerNumber;
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
