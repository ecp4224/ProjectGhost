using System.Drawing;
using System.Linq;
using Ghost.Core;

namespace Ghost
{
    public class Players
    {
        private static Entity[] _entities = new Entity[4];
        public static readonly Color[] PlayerColors =
        {
            Color.FromArgb(255, 197, 0, 0),
            Color.FromArgb(255, 0, 81, 197),
            Color.FromArgb(255, 0, 159, 0),
            Color.FromArgb(255, 1, 216, 0)
        };

        /*public static Entity GetPlayer(int playerNumber)
        {
            return _entities[playerNumber - 1];
        }

        public static Entity CreateInputPlayer()
        {
            int slot = NextOpenSlot();
            if (slot == -1)
                return null;

            IInput input = GamepadInput.CreateInputFor(slot);
            if (slot == 1)
                input = GamepadKeyboardInput.GamepadKeyboardInstance;

            if (IsInputUsed(input))
                return null;

            var player = new InputEntity(slot, input);
            _entities[slot - 1] = player;
            return player;
        }

        public static Entity CreateInputPlayer(IInput input)
        {
            if (IsInputUsed(input))
                return null;

            int slot = NextOpenSlot();
            if (slot == -1)
                return null;

            var player = new InputEntity(slot, input);
            _entities[slot - 1] = player;
            return player;
        }

        public static int MaxSlots()
        {
            return _entities.Length;
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
                if (_entities[i] == null)
                    break;
            }

            return i >= 4 ? -1 : i + 1;
        }

        public static bool IsInputUsed(IInput input)
        {
            foreach (var player in _entities.OfType<InputEntity>())
            {
                if (player == null) continue;
                if (player.Input.Equals(input)) return false;
            }

            return false;
        }

        public static Entity CreateNetworkPlayer()
        {
            int slot = NextOpenSlot();
            if (slot == -1)
                return null;

            var player = new NetworkPlayer(slot);
            _entities[slot - 1] = player;
            return player;
        }

        public static Entity GetPlayer1()
        {
            return GetPlayer(1) ?? CreateInputPlayer(GamepadKeyboardInput.GamepadKeyboardInstance);
        }

        public static Entity GetPlayer2()
        {
            return GetPlayer(2) ?? CreateNetworkPlayer();
        }*/
    }
}
