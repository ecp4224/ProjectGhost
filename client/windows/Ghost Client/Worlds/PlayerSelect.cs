using System.Drawing;
using Ghost.Core;
using OpenTK.Input;
using Sharp2D;
using Sharp2D.Core.Interfaces;

namespace Ghost.Worlds
{
    public class PlayerSelect : GenericWorld, ILogical
    {
        public bool WaitingForNetwork = false;
        public override string Name
        {
            get { return "PlayerSelect"; }
        }

        protected override void OnDisplay()
        {
            base.OnDisplay();

            var sprite  = Text.CreateTextSprite("Press start/space to join!", Color.White, new Font(Program.RetroFont, 32));
            var sprite2 = Text.CreateTextSprite("And Enter/A to join the server!", Color.White,
                new Font(Program.RetroFont, 22));

            sprite.X = -Screen.Camera.X + ((sprite.Width - sprite.StringWidth) / 2f);
            sprite2.X = -Screen.Camera.X + ((sprite2.Width - sprite2.StringWidth)/2f);
            sprite2.Y = -140f;
            sprite.Y = -180F;
            AddSprite(sprite);
            AddSprite(sprite2);
            AddLogical(this);
        }

        public void Update()
        {
            var kstate = Keyboard.GetState();
            if (!WaitingForNetwork && kstate.IsKeyDown(Key.Space) && !Players.IsInputUsed(KeyboardInput.KeyboardInstance))
            {
                var player = Players.CreateInputPlayer(KeyboardInput.KeyboardInstance);
                if (player == null)
                    return;
                ShowPlayerIcon(player.PlayerNumber, true);
            }
            else if (kstate.IsKeyDown(Key.Enter))
            {
                WaitForServer();
            }

            var states = GamepadHelper.GetAllStates();
            for (int i = 0; i < states.Length; i++)
            {
                var input = GamepadInput.CreateInputFor(i + 1);

                var state = states[i];
                if (!WaitingForNetwork && state.Buttons.Start == ButtonState.Pressed)
                {
                    if (Players.IsInputUsed(input))
                        continue;

                    var player = Players.CreateInputPlayer(input);
                    if (player == null)
                        continue;
                    ShowPlayerIcon(player.PlayerNumber);
                }
                else if (state.Buttons.A == ButtonState.Pressed)
                {
                    WaitForServer();
                }
            }
        }

        public void WaitForServer()
        {
            if (WaitingForNetwork)
                return;

            WaitingForNetwork = true;

            var sprite = Text.CreateTextSprite("Waiting for opposing team..", Color.White, new Font(Program.RetroFont, 18));
            sprite.X = -Screen.Camera.X + ((sprite.Width - sprite.StringWidth) / 2f);
            sprite.Y = 130f;

            AddSprite(sprite);

            //TODO Actually ready up
        }

        public void ShowPlayerIcon(int playerNumber, bool keyboard = false)
        {
            var sprite = Sprite.FromImage("sprites/" + (keyboard ? "keyboard.png" : "controller.png"));
            sprite.TintColor = Players.PlayerColors[playerNumber - 1];
            sprite.Y = Screen.Camera.Y;
            sprite.X = ((playerNumber - 2f) * 130f) - 68f;

            AddSprite(sprite);
        }
    }
}
