using System.Drawing;
using System.Linq;
using Ghost.Core;

namespace Ghost
{
    public class Players
    {
        private static Player[] players = new Player[4];
        public static readonly Color[] PlayerColors =
        {
            Color.FromArgb(1, 197, 0, 0),
            Color.FromArgb(1, 0, 81, 197),
            Color.FromArgb(1, 0, 159, 0),
            Color.FromArgb(1, 1, 216, 0)
        };

        public static Player GetPlayer(int playerNumber)
        {
            return players[playerNumber - 1];
        }

        public static Player CreateInputPlayer()
        {
            int slot = NextOpenSlot();
            if (slot == -1)
                return null;

            IInput input = GamepadInput.CreateInputFor(slot);
            if (slot == 1)
                input = GamepadKeyboardInput.GamepadKeyboardInstance;

            if (IsInputUsed(input))
                return null;

            var player = new InputPlayer(slot, input);
            players[slot - 1] = player;
            return player;
        }

        public static Player CreateInputPlayer(IInput input)
        {
            if (IsInputUsed(input))
                return null;

            int slot = NextOpenSlot();
            if (slot == -1)
                return null;

            var player = new InputPlayer(slot, input);
            players[slot - 1] = player;
            return player;
        }

        public static int MaxSlots()
        {
            return players.Length;
        }

        public static int OpenSlots()
        {
            return MaxSlots() - PlayerCount();
        }

        public static int PlayerCount()
        {
            return NextOpenSlot() - 1;
        }

        public static int NextOpenSlot()
        {
            int i = 0;
            for (; i < 4; i++)
            {
                if (players[i] == null)
                    break;
            }

            return i >= 4 ? -1 : i + 1;
        }

        public static bool IsInputUsed(IInput input)
        {
            foreach (var player in players.OfType<InputPlayer>())
            {
                if (player == null) continue;
                if (player.Input.Equals(input)) return false;
            }

            return false;
        }
    }
}
