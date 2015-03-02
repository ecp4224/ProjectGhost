using System;
using System.Collections.Generic;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using Ghost.Core;
using Ghost.Core.Network;
using OpenTK.Input;
using Sharp2D;
using Sharp2D.Core.Interfaces;

namespace Ghost.Worlds
{
    public class QueueWorld : GenericWorld
    {
        private TextSprite textSprite;
        private Player player1;
        private NetworkPlayer player2;
        public override string Name
        {
            get { return "queue"; }
        }

        protected override void OnLoad()
        {
            base.OnLoad();

            Server.JoinQueue(QueueType.Random);
            if (Server.WaitForOk())
            {
                Server.OnMatchFound(delegate(MatchInfo info)
                {
                    var sprite = Text.CreateTextSprite("A match has been found!", Color.White, new Font(Program.RetroFont, 18));
                    var sprite2 = Text.CreateTextSprite("Please wait..", Color.White, new Font(Program.RetroFont, 18));

                    RemoveSprite(textSprite);

                    sprite.X = -Screen.Camera.X + ((sprite.Width - sprite.StringWidth) / 2f);
                    sprite2.X = -Screen.Camera.X + ((sprite2.Width - sprite2.StringWidth) / 2f);
                    sprite.Y = 130f;
                    sprite2.Y = 150f;
                    sprite2.NeverClip = true;

                    AddSprite(sprite);
                    AddSprite(sprite2);

                    Thread.Sleep(5000);

                    player1.XVel = 0f;
                    player1.YVel = 0f;
                    player1.X = info.startX;
                    player1.Y = info.startY;

                    player2 = new NetworkPlayer(2)
                    {
                        TintColor = Players.PlayerColors[0],
                        X = info.opStartX,
                        Y = info.opStartY
                    };

                    AddSprite(player2);

                    var username = Text.CreateTextSprite(info.opUsername, Color.White, new Font(Program.RetroFont, 18));
                    username.Y = player2.Y - 32f;
                    username.X = player2.X + ((username.Width - username.StringWidth)/2f);
                    username.NeverClip = true;
                    player2.Attach(username);
                    AddSprite(username);

                    Server.isInMatch = true;

                    RemoveSprite(sprite);
                    RemoveSprite(sprite2);
                });
            }
        }

        protected override void OnDisplay()
        {
            base.OnDisplay();

            textSprite = Text.CreateTextSprite("You are currently in match queueing...", Color.White, new Font(Program.RetroFont, 18));
            textSprite.X = -Screen.Camera.X + ((textSprite.Width - textSprite.StringWidth) / 2f);
            textSprite.Y = 130f;

            AddSprite(textSprite);

            player1 = Players.GetPlayer1();
            player1.TintColor = Players.PlayerColors[1];
            player1.X = -Screen.Camera.X;
            player1.Y = Screen.Camera.Y;

            AddSprite(player1);
        }
    }
}
