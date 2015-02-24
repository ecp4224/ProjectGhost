using Ghost.Core;
using OpenTK.Input;

namespace Ghost
{
    public class InputPlayer : Player
    {
        
        private readonly IInput _input;
        public bool HasController
        {
            get { return _input.IsConnected; }
        }

        public IInput Input
        {
            get { return _input; }
        }

        public InputPlayer(int playerNumber, IInput input) : base(playerNumber)
        {
            _input = input;
        }

        protected override void OnInput()
        {
            var movementVector = _input.CalculateMovement();

            XVel = movementVector.X;
            YVel = movementVector.Y;
        }
    }
}
