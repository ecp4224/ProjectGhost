using System;
using Sharp2D;
using Sharp2D.Core.Interfaces;

namespace Ghost.Core
{
    public static class AnimationHelper
    {
        public static DynamicAnimation CreateDynamicAnimation(Action action, Func<bool> stopCondition = null)
        {
            return new DynamicAnimation(action, stopCondition);
        }

        public static DynamicAnimation CreateDynamicAnimation(Action action, long duration)
        {
            return new DynamicAnimation(action, duration);
        }

        public static DynamicAnimation CreateDynamicAnimation(Action<long> action, Func<bool> stopCondition = null)
        {
            return new DynamicAnimation(action, stopCondition);
        }

        public static DynamicAnimation CreateDynamicAnimation(Action<long> action, long duration)
        {
            return new DynamicAnimation(action, duration);
        }
    }

    public class DynamicAnimation : ILogical
    {
        private Action _action;
        private Action _ended;
        private Action<long> _action_with_elaspe;
        private Func<bool> _stopFunc;
        private long _startTime;
        private ILogicContainer _currentContainer;

        public bool HasEnded { get; private set; }

        public long Elaspe
        {
            get { return Environment.TickCount - _startTime; }
        }

        internal DynamicAnimation(Action action, Func<bool> stopFunc)
        {
            this._action = action;
            this._stopFunc = stopFunc;
        }

        internal DynamicAnimation(Action action, long duration)
        {
            this._action = action;
            this._stopFunc = () => Environment.TickCount - _startTime >= duration;
        }

        internal DynamicAnimation(Action action)
        {
            this._action = action;
            this._stopFunc = null;
        }

        internal DynamicAnimation(Action<long> action, Func<bool> stopFunc)
        {
            this._action_with_elaspe = action;
            this._stopFunc = stopFunc;
        }

        internal DynamicAnimation(Action<long> action, long duration)
        {
            this._action_with_elaspe = action;
            this._stopFunc = () => Environment.TickCount - _startTime >= duration;
        }

        internal DynamicAnimation(Action<long> action)
        {
            this._action_with_elaspe = action;
            this._stopFunc = null;
        }

        public DynamicAnimation Until(Func<bool> stopFunc)
        {
            this._stopFunc = stopFunc;
            return this;
        }

        public DynamicAnimation Start()
        {
            return Start(GhostClient.Ghost.CurrentGhostGame);
        }

        public DynamicAnimation Start(ILogicContainer world)
        {
            world.AddLogical(this);
            _startTime = Environment.TickCount;
            _currentContainer = world;
            return this;
        }

        public DynamicAnimation End()
        {
            _currentContainer.RemoveLogical(this);
            HasEnded = true;
            
            if (_ended != null)
                _ended();

            return this;
        }

        public DynamicAnimation OnEnded(Action action)
        {
            _ended = action;
            return this;
        }

        public void Dispose()
        {
            _currentContainer = null;
            _action = null;
            _stopFunc = null;
        }

        public void Update()
        {
            if (HasEnded)
                return;

            if (_action != null)
                _action();
            if (_action_with_elaspe != null)
                _action_with_elaspe(Environment.TickCount - _startTime);

            if (_stopFunc != null && _stopFunc())
                End();
        }
    }
}
