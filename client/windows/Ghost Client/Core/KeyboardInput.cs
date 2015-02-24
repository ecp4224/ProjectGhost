using OpenTK;
using OpenTK.Input;

namespace Ghost.Core
{
    public class KeyboardInput : IInput 
    {
        public static readonly KeyboardInput KeyboardInstance = new KeyboardInput();

        private KeyboardInput()
        {
        }

        public bool IsConnected
        {
            get { return Keyboard.GetState().IsConnected; }
        }

        public Vector2 CalculateMovement()
        {
            var keyboard = Keyboard.GetState();
            var toReturn = new Vector2();
            toReturn.X += keyboard.IsKeyDown(Key.D) ? 1 : 0;
            toReturn.X -= keyboard.IsKeyDown(Key.A) ? -1 : 0;
            toReturn.Y += keyboard.IsKeyDown(Key.W) ? 1 : 0;
            toReturn.Y -= keyboard.IsKeyDown(Key.S) ? -1 : 0;

            return toReturn;
        }

        public bool CheckInputFor(Player player)
        {
            var state = Keyboard.GetState();
            return false;
        }

        public bool Equals(IInput input)
        {
            return input is KeyboardInput;
        }
    }
}
