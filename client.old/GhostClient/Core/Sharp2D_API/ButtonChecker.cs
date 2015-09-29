using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Xna.Framework.Input;

namespace Ghost.Core.Sharp2D_API
{
    public class ButtonChecker
    {
        private static Dictionary<Keys, bool> isHeld = new Dictionary<Keys, bool>();
 
        public static void CheckAndDebounceKey(KeyboardState state, Keys key, Action action)
        {
            if (state.IsKeyDown(key) && (!isHeld.ContainsKey(key) || !isHeld[key]))
            {
                action();

                if (!isHeld.ContainsKey(key))
                {
                    isHeld.Add(key, true);
                }
                else isHeld[key] = true;
            }
            else if (!state.IsKeyDown(key) && isHeld.ContainsKey(key) && isHeld[key])
            {
                isHeld[key] = false;
            }
        }
    }
}
