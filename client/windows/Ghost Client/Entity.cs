using System;
using System.Linq;
using System.Xml;
using Ghost.Core;
using Sharp2D;
using Sharp2D.Core.Interfaces;

namespace Ghost
{
    public abstract class Entity : PhysicsSprite
    {
        private float _xVel, _yVel;
        private bool paused;
        private float oXVel, oYVel;

        private bool interpolate;
        private float inter_targetX, inter_targetY, inter_startX, inter_startY, inter_timeTakes;
        private int inter_timeStart;

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

        public void Pause()
        {
            if (paused)
                return;
            paused = true;
            oXVel = _xVel;
            oYVel = _yVel;
        }

        public void UnPause()
        {
            if (!paused)
                return;
            paused = false;
            _xVel = oXVel;
            _yVel = oYVel;
        }

        public void InterpolateTo(float x, float y, float duration)
        {
            inter_targetX = x;
            inter_targetY = y;
            inter_startX = X;
            inter_startY = Y;
            inter_timeStart = Screen.TickCount;
            inter_timeTakes = duration;
            interpolate = true;
        }

        public float TargetX { get; set; }

        public float TargetY { get; set; }

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
                        float a = MathUtils.Ease(1f, 0f, 700, _currentAnimation.Elaspe);
                        Alpha = a;
                        foreach (var s in Children.OfType<Sprite>())
                        {
                            s.Alpha = a;
                        }
                    }).Until(() => Alpha == 0f)
                        .OnEnded(() =>
                        {
                            Alpha = 0f;
                            foreach (var s in Children.OfType<Sprite>())
                            {
                                s.Alpha = 0;
                            }
                        })
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
                    foreach (var s in Children.OfType<Sprite>())
                    {
                        s.Alpha = 1f;
                    }
                }
            }
        }

        public override string Name
        {
            get { return "Entity-" + ID; }
        }

        public short ID { get; private set; }

        protected Entity(short id)
        {
            this.ID = id;
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

            if (!interpolate)
            {
                if (Math.Abs(X - TargetX) < 8 && Math.Abs(Y - TargetY) < 8)
                {
                    XVel = 0f;
                    YVel = 0f;
                }

                X += XVel;
                Y += YVel;
            }
            else
            {
                X = MathUtils.Ease(inter_startX, inter_targetX, inter_timeTakes, Screen.TickCount - inter_timeStart);
                Y = MathUtils.Ease(inter_startY, inter_targetY, inter_timeTakes, Screen.TickCount - inter_timeStart);

                if (Math.Abs(X - inter_targetX) < 4 && Math.Abs(Y - inter_targetY) < 4)
                {
                    interpolate = false;
                }
            }
        }
    }
}
