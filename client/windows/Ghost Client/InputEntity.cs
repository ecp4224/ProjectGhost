using System;
using System.Drawing;
using System.IO;
using System.Threading;
using Ghost.Core;
using Ghost.Core.Network;
using Ghost.Worlds;
using OpenTK;
using OpenTK.Input;
using Sharp2D;

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

            Screen.NativeWindow.MouseDown += NativeWindowOnMouseDown;
        }

        protected override void OnUnload()
        {
            base.OnUnload();

            Screen.NativeWindow.MouseDown -= NativeWindowOnMouseDown;
        }

        private void NativeWindowOnMouseDown(object sender, MouseButtonEventArgs mouseButtonEventArgs)
        {
            if (!mouseButtonEventArgs.IsPressed)
                return;

            if (Frozen)
                return;

            float targetX = mouseButtonEventArgs.X - Screen.Camera.X - (Screen.Settings.GameSize.Width / 2f);
            float targetY = mouseButtonEventArgs.Y + Screen.Camera.Y - (Screen.Settings.GameSize.Height / 2f);

            if (!Server.useWASD && mouseButtonEventArgs.Button == MouseButton.Left)
            {
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

                var feedback = new FeedbackCircle()
                {
                    X = targetX,
                    Y = targetY
                };

                CurrentWorld.AddSprite(feedback);
            }
            else if (mouseButtonEventArgs.Button == MouseButton.Right)
            {
                if (Server.matchStarted)
                {
                    new Thread(new ThreadStart(delegate
                    {
                        Server.FireRequest(targetX, targetY);
                    })).Start(); //TODO Buffer this...
                }
            }
        }

        private float lastTargetX = -900, lastTargetY = -900;
        private void CheckWASD()
        {
            var keyboard = Keyboard.GetState();

            float targetX = X, targetY = Y;

            if (keyboard.IsKeyDown(Key.W))
            {
                targetY = -350;
            }
            if (keyboard.IsKeyDown(Key.A))
            {
                targetX = -504;
            }
            if (keyboard.IsKeyDown(Key.S))
            {
                targetY = 350;
            }
            if (keyboard.IsKeyDown(Key.D))
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

            if (Server.useWASD)
            {
                CheckWASD();
            }

            if (Server.isInMatch && !Server.isReady)
            {
                var state = Keyboard.GetState();
                if (state.IsKeyDown(Key.Space))
                {
                    Server.isReady = true;
                    Server.SendReady();
                    CurrentWorld.RemoveSprite(QueueWorld.readyText);

                    QueueWorld.readyText = Text.CreateTextSprite("Ready! Please wait for game to start..", Color.White,
                        new Font(Program.RetroFont, 18));
                    QueueWorld.readyText.X = -Screen.Camera.X + ((QueueWorld.readyText.Width - QueueWorld.readyText.StringWidth) / 2f);
                    QueueWorld.readyText.Y = 130f;
                    CurrentWorld.AddSprite(QueueWorld.readyText);
                }
            }
        }
    }
}
