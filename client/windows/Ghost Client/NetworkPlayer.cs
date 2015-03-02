using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using Ghost.Core.Network;
using Sharp2D;

namespace Ghost
{
    public class NetworkPlayer : Player
    {
        private bool running = false;
        private Thread packetThread;
        private int lastPacketRead = 0;
        public NetworkPlayer(int playerNumber) : base(playerNumber)
        {
        }

        protected override void OnLoad()
        {
            base.OnLoad();

            Texture = Texture.NewTexture("sprites/ball.png");
            if (!Texture.Loaded)
                Texture.LoadTextureFromFile();

            Width = Texture.TextureWidth;
            Height = Texture.TextureHeight;

            running = true;
            packetThread = new Thread(new ThreadStart(delegate
            {
                while (running)
                {
                    GetPackets();
                }
            }));
            packetThread.Start();

            NeverClip = true;
        }

        protected override void OnInput() { }

        protected override void OnUnload()
        {
            base.OnUnload();

            running = false;
            packetThread.Interrupt();
        }

        private void GetPackets()
        {
            byte[] data = Server.UdpClient.Receive(ref Server.ServerEndPoint);
            if (data.Length >= 21 && data[0] == 0x04)
            {
                int packetNumber = BitConverter.ToInt32(data, 1);
                if (packetNumber < lastPacketRead)
                {
                    int dif = lastPacketRead - packetNumber;
                    if (dif >= int.MaxValue - 1000)
                    {
                        lastPacketRead = packetNumber;
                    }
                    else return;
                }
                else
                {
                    lastPacketRead = packetNumber;
                }
                 

                float x = BitConverter.ToSingle(data, 5);
                float y = BitConverter.ToSingle(data, 9);
                float xvel = BitConverter.ToSingle(data, 13);
                float yvel = BitConverter.ToSingle(data, 17);

                XVel = xvel;
                YVel = yvel;
                X = x;
                Y = y;
            }
        }
    }
}
