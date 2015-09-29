using System;
using System.Drawing;
using System.Threading;
using Ghost.Core;
using Ghost.Core.Network;
using GhostClient.Core;
using Microsoft.Xna.Framework.Input;

namespace Ghost
{
    public class InputEntity : NetworkPlayer
    {
        private const float SPEED = 7f;

        public InputEntity(short id) : base(id, "")
        {
        }

        private double fireRate, speedStat;

        public double FireRateStat
        {
            get { return fireRate; }
            set
            {
                fireRate = value;
                UpdateStats();
            }
        }

        public double SpeedStat
        {
            get { return speedStat; }
            set
            {
                speedStat = value;
                UpdateStats();
            }
        }

        protected override void OnLoad()
        {
            base.OnLoad();

            TintColor = System.Drawing.Color.FromArgb(255, 0, 81, 197);
        }

        private TextSprite stats;
        private void UpdateStats()
        {
            if (stats == null)
            {
                stats = TextSprite.CreateText("Speed: " + SpeedStat + "\n" +
                                              "FireRate: " + FireRateStat + "\n", "BigRetro");
                CurrentWorld.AddSprite(stats);
                stats.X = (stats.Width/2f) + 50f;
                stats.Y = 720 - (stats.Height / 2f);
            }
            else
            {
                stats.Text = "Speed: " + SpeedStat + "\n" +
                             "FireRate: " + FireRateStat + "\n";
            }
        }

        private bool leftMouse, rightMouse;
        private void CheckMouse()
        {
            var mouse = Mouse.GetState();
            var keyboard = Keyboard.GetState();

            if (keyboard.IsKeyDown(Keys.Escape))
            {
                Environment.Exit(0);
                return;
            }

            float targetX = mouse.X;
            float targetY = mouse.Y;

            if (GhostClient.Ghost.CurrentGhostGame.Graphics.IsFullScreen)
            {
                targetX = targetX/GhostClient.Ghost.CurrentGhostGame.WidthScale;
                targetY = targetY/GhostClient.Ghost.CurrentGhostGame.HeightScale;
            }

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

            /*if (Server.isInMatch && !Server.isReady)
            {
                var state = Keyboard.GetState();
                if (state.IsKeyDown(Keys.Space))
                {
                    Server.isReady = true;
                    Server.SendReady();

                    /*CurrentWorld.RemoveSprite(GameHandler.readyText);

                    GameHandler.readyText = TextSprite.CreateText("Ready! Please wait for game to start..", "Retro");
                    GameHandler.readyText.X = 512F;
                    GameHandler.readyText.Y = 590F;
                    CurrentWorld.AddSprite(GameHandler.readyText);#1#
                }
            }*/
        }
    }
}
