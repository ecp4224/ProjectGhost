using System;
using System.Threading;
using Ghost.Core.Network;
using Microsoft.Xna.Framework.Input;

namespace Ghost
{
    public class InputEntity : NetworkPlayer
    {
        private const float SPEED = 7f;

        public InputEntity(short id) : base(id, "")
        {
        }

        protected override void OnLoad()
        {
            base.OnLoad();

            TintColor = System.Drawing.Color.FromArgb(255, 0, 81, 197);
        }

        private bool leftMouse, rightMouse;
        private void CheckMouse()
        {
            var mouse = Mouse.GetState();

            float targetX = mouse.X;
            float targetY = mouse.Y;

            if (mouse.LeftButton == ButtonState.Pressed && !leftMouse)
            {
                leftMouse = true;

                if (Server.matchStarted)
                {
                    new Thread(new ThreadStart(delegate
                    {
                        Server.MovementRequest(targetX, targetY);
                    })).Start(); //TODO Buffer this...
                }
                else if (!Server.isInMatch)
                {
                    float asdx = targetX - X;
                    float asdy = targetY - Y;
                    float inv = (float)Math.Atan2(asdy, asdx);


                    XVel = (float)(Math.Cos(inv) * SPEED);
                    YVel = (float)(Math.Sin(inv) * SPEED);
                    TargetX = targetX;
                    TargetY = targetY;
                }

                var feedback = new FeedbackCircle()
                {
                    X = targetX,
                    Y = targetY
                };

                CurrentWorld.AddSprite(feedback);

            } else if (mouse.LeftButton == ButtonState.Released && leftMouse)
                leftMouse = false;

            if (mouse.RightButton == ButtonState.Pressed && !leftMouse)
            {
                rightMouse = true;

                if (Server.matchStarted)
                {
                    new Thread(new ThreadStart(delegate
                    {
                        Server.FireRequest(targetX, targetY);
                    })).Start(); //TODO Buffer this...
                }
            }
            else if (mouse.RightButton == ButtonState.Released && leftMouse)
                rightMouse = false;
        }

        private float lastTargetX = -900, lastTargetY = -900;
        private void CheckWASD()
        {
            var keyboard = Keyboard.GetState();

            float targetX = X, targetY = Y;

            if (keyboard.IsKeyDown(Keys.W))
            {
                targetY = -350;
            }
            if (keyboard.IsKeyDown(Keys.A))
            {
                targetX = -504;
            }
            if (keyboard.IsKeyDown(Keys.S))
            {
                targetY = 350;
            }
            if (keyboard.IsKeyDown(Keys.D))
            {
                targetX = 504;
            }
            if (lastTargetX != targetX || lastTargetY != targetY)
            {
                lastTargetX = targetX;
                lastTargetY = targetY;

                if (Server.matchStarted)
                {
                    new Thread(new ThreadStart(delegate
                    {
                        Server.MovementRequest(targetX, targetY);
                    })).Start(); //TODO Buffer this...
                }
                else if (!Server.isInMatch)
                {
                    float asdx = targetX - X;
                    float asdy = targetY - Y;
                    float inv = (float) Math.Atan2(asdy, asdx);


                    XVel = (float) (Math.Cos(inv)*SPEED);
                    YVel = (float) (Math.Sin(inv)*SPEED);
                    TargetX = targetX;
                    TargetY = targetY;
                }
            }
        }

        public override void Update()
        {
            base.Update();

            if (GhostClient.Ghost.CurrentGhostGame.IsActive)
            {
                if (Server.useWASD)
                {
                    CheckWASD();
                }
                else
                {
                    CheckMouse();
                }
            }

            if (Server.isInMatch && !Server.isReady)
            {
                var state = Keyboard.GetState();
                if (state.IsKeyDown(Keys.Space))
                {
                    Server.isReady = true;
                    Server.SendReady();
                    
                    //TODO Remove readyup message

                    /*CurrentWorld.RemoveSprite(QueueWorld.readyText);

                    QueueWorld.readyText = Text.CreateTextSprite("Ready! Please wait for game to start..", System.Drawing.Color.White,
                        new Font(Program.RetroFont, 18));
                    QueueWorld.readyText.X = -Screen.Camera.X + ((QueueWorld.readyText.Width - QueueWorld.readyText.StringWidth) / 2f);
                    QueueWorld.readyText.Y = 130f;
                    CurrentWorld.AddSprite(QueueWorld.readyText);*/
                }
            }
        }
    }
}
