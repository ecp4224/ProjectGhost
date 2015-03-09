using OpenTK;
using OpenTK.Input;

namespace Ghost.Core
{
    public class GamepadInput : IInput
    {
        public static GamepadInput CreateInputFor(int player)
        {
            return new GamepadInput(player - 1);
        }

        private readonly int _slot;

        private GamepadInput(int slot)
        {
            this._slot = slot;
        }

        public bool IsConnected
        {
            get { return GamePad.GetState(_slot).IsConnected; }
        }

        public Vector2 CalculateMovement()
        {
            var gstate = GamePad.GetState(_slot);
            Vector2 toReturn = gstate.ThumbSticks.Left;
            toReturn.Y = gstate.Buttons.A == ButtonState.Pressed ? -1 : 0;
            if (toReturn.LengthSquared < 0.3)
                toReturn = new Vector2(0, 0);

            return toReturn;
        }

        public bool CheckInputFor(Entity entity)
        {
            var gstate = GamePad.GetState(_slot);
            return false;
        }

        public bool Equals(IInput input)
        {
            var gamepadInput = input as GamepadInput;
            if (gamepadInput != null)
                return gamepadInput._slot == _slot;
            return false;
        }
    }
}
