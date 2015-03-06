using System;
using System.IO;
using Ghost.Core;
using Ghost.Core.Network;
using OpenTK;
using OpenTK.Input;
using Sharp2D;

namespace Ghost
{
    public class InputPlayer : Player
    {
        private readonly IInput _input;

        private bool pathing = false;
        private float targetX, targetY;
        private const float SPEED = 7f;

        private Vector2 lastVelocity;
        private int packetWriteNumber = 0;
        private const float SendTolerence = 0.05f;

        public bool HasController
        {
            get { return _input.IsConnected; }
        }

        public IInput Input
        {
            get { return _input; }
        }

        public InputPlayer(int playerNumber, IInput input) : base(playerNumber)
        {
            _input = input;
        }

        protected override void OnLoad()
        {
            base.OnLoad();

            Texture = Texture.NewTexture("sprites/ball.png");
            if (!Texture.Loaded)
                Texture.LoadTextureFromFile();

            Width = Texture.TextureWidth;
            Height = Texture.TextureHeight;

            Screen.NativeWindow.MouseDown += NativeWindowOnMouseDown;

            lastVelocity = new Vector2(XVel, YVel);

            NeverClip = true;
        }

        private void NativeWindowOnMouseDown(object sender, MouseButtonEventArgs mouseButtonEventArgs)
        {
            if (!mouseButtonEventArgs.IsPressed)
                return;

            pathing = true;
            targetX = mouseButtonEventArgs.X - Screen.Camera.X - (Screen.Settings.GameSize.Width / 2f);
            targetY = mouseButtonEventArgs.Y + Screen.Camera.Y - (Screen.Settings.GameSize.Height / 2f);
            /*float asdx = targetX - X;
            float asdy = targetY - Y;
            float inv = (float) Math.Atan2(asdy, asdx);


            XVel = (float) (Math.Cos(inv)*SPEED);
            YVel = (float) (Math.Sin(inv)*SPEED);*/
        }

        protected override void OnInput()
        {
            if (pathing)
            {
                if (Math.Abs(X - targetX) < 8)
                    XVel = 0;
                if (Math.Abs(Y - targetY) < 8)
                    YVel = 0;

                pathing = XVel != 0 || YVel != 0;
            }
            var movementVector = _input.CalculateMovement();

            if (movementVector.LengthSquared > 0.3 && pathing)
                pathing = false;

            if (!pathing)
            {
                XVel = movementVector.X * SPEED;
                YVel = movementVector.Y * SPEED;
            }
        }

        private void ReadPacket()
        {
            byte[] data = Server.UdpClient.Receive(ref Server.ServerEndPoint);
            if (data[0] == 0x09) //Ping!
            {
                Server.EndPingTimer();
                Console.WriteLine("Ping: " + Server.GetLatency());
            }
            else if (data[0] == 0x04)
            {
                
            }
        }

        protected override void OnMovement()
        {
            base.OnMovement();

            var currentVelocity = new Vector2(XVel, YVel);

            if (Server.isInMatch && (Math.Abs(XVel - lastVelocity.X) > SendTolerence || Math.Abs(YVel - lastVelocity.Y) > SendTolerence))
            {
                packetWriteNumber++;
                if (int.MaxValue - packetWriteNumber < 300)
                {
                    packetWriteNumber = 0;
                }

                byte[] data = new byte[21];
                data[0] = 0x04;
                byte[] packetNum = BitConverter.GetBytes(packetWriteNumber);
                byte[] xBytes = BitConverter.GetBytes(X);
                byte[] yBytes = BitConverter.GetBytes(Y);
                byte[] xVelBytes = BitConverter.GetBytes(XVel);
                byte[] yVelBytes = BitConverter.GetBytes(YVel);

                Array.Copy(packetNum, 0, data, 1, packetNum.Length);
                Array.Copy(xBytes, 0, data, 5, xBytes.Length);
                Array.Copy(yBytes, 0, data, 9, yBytes.Length);
                Array.Copy(xVelBytes, 0, data, 13, xVelBytes.Length);
                Array.Copy(yVelBytes, 0, data, 17, yVelBytes.Length);

                Server.UdpClient.Send(data, data.Length);
            }

            lastVelocity = currentVelocity;
        }
    }
}
