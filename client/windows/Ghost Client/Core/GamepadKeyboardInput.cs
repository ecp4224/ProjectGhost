using OpenTK;

namespace Ghost.Core
{
    public class GamepadKeyboardInput : IInput
    {
        public static readonly GamepadKeyboardInput GamepadKeyboardInstance = new GamepadKeyboardInput();

        private GamepadInput gamepad;
        private KeyboardInput keyboard;
        private GamepadKeyboardInput()
        {
            gamepad = GamepadInput.CreateInputFor(1);
            keyboard = KeyboardInput.KeyboardInstance;
        }


        public bool IsConnected { get; private set; }
        public Vector2 CalculateMovement()
        {
            var movement = gamepad.CalculateMovement();
            if (movement.LengthSquared < 0.3)
                movement = keyboard.CalculateMovement();

            return movement;
        }

        public bool CheckInputFor(Entity entity)
        {
            bool changed = gamepad.CheckInputFor(entity);
            if (!changed)
                changed = keyboard.CheckInputFor(entity);

            return changed;
        }

        public bool Equals(IInput input)
        {
            return input is GamepadKeyboardInput;
        }
    }
}
