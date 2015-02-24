using System.Collections.Generic;
using OpenTK.Input;

namespace Ghost.Core
{
    public static class GamepadHelper
    {
        public static GamePadState[] GetAllStates()
        {
            var states = new List<GamePadState>();
            for (int i = 0; i < 128; i++)
            {
                var state = GamePad.GetState(i);
                if (state.IsConnected)
                    states.Add(state);
                else
                    break;
            }

            return states.ToArray();
        }
    }
}
