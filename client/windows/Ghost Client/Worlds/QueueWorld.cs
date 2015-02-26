using System;
using System.Collections.Generic;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Ghost.Core;
using Ghost.Core.Network;
using Sharp2D;

namespace Ghost.Worlds
{
    public class QueueWorld : GenericWorld
    {
        public override string Name
        {
            get { return "queue"; }
        }

        protected override void OnLoad()
        {
            base.OnLoad();

            Server.JoinQueue(QueueType.Random);
            Server.OnMatchFound(delegate(MatchInfo info)
            {
                Console.WriteLine("Match found " + info);
            });
        }

        protected override void OnDisplay()
        {
            base.OnDisplay();

            var sprite = Text.CreateTextSprite("You are currently in match queueing...", Color.White, new Font(Program.RetroFont, 18));
            sprite.X = -Screen.Camera.X + ((sprite.Width - sprite.StringWidth) / 2f);
            sprite.Y = 130f;

            AddSprite(sprite);

            Player player = Players.CreateInputPlayer(GamepadKeyboardInput.GamepadKeyboardInstance);
            player.TintColor = Players.PlayerColors[1];
            player.X = -Screen.Camera.X;
            player.Y = Screen.Camera.Y;

            AddSprite(player);
        }
    }
}
