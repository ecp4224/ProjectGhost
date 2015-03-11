using System;
using System.Collections.Generic;
using System.Drawing;
using System.Linq;
using System.Net.NetworkInformation;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using Ghost.Core;
using Ghost.Core.Network;
using OpenTK.Graphics.ES11;
using OpenTK.Input;
using Sharp2D;
using Sharp2D.Core.Interfaces;

namespace Ghost.Worlds
{
    public class QueueWorld : GenericWorld
    {
        public static TextSprite readyText;
        private TextSprite textSprite;
        private Entity player1;

        private Thread tcpThread;
        private Thread udpThread;
        private Thread pingThread;

        private Dictionary<short, Entity> entities = new Dictionary<short, Entity>(); 

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

                    Server.isInMatch = true;
                    Server.isReady = false;
                    Server.matchStarted = false;

                    tcpThread.Start();
                    udpThread.Start();

                    RemoveSprite(sprite);
                    RemoveSprite(sprite2);

                    readyText = Text.CreateTextSprite("Press space to ready up!", Color.White,
                        new Font(Program.RetroFont, 18));
                    readyText.X = -Screen.Camera.X + ((readyText.Width - readyText.StringWidth) / 2f);
                    readyText.Y = 130f;
                    AddSprite(readyText);
                });
            }
        }

        protected override void OnInitialDisplay()
        {
            base.OnInitialDisplay();

            textSprite = Text.CreateTextSprite("You are currently in match queueing...", Color.White, new Font(Program.RetroFont, 18));
            textSprite.X = -Screen.Camera.X + ((textSprite.Width - textSprite.StringWidth) / 2f);
            textSprite.Y = 130f;

            AddSprite(textSprite);

            player1 = new InputEntity(0)
            {
                TintColor = Players.PlayerColors[1],
                X = -Screen.Camera.X,
                Y = Screen.Camera.Y
            };

            AddSprite(player1);

            tcpThread = new Thread(new ThreadStart(delegate
            {
                Server.TcpStream.ReadTimeout = Timeout.Infinite;
                while (Server.isInMatch)
                {
                    ReadTcpPackets();
                }
            }));
            udpThread = new Thread(new ThreadStart(delegate
            {
                while (Server.isInMatch)
                {
                    ReadUdpPackets();
                }
            }));

            pingThread = new Thread(new ThreadStart(delegate
            {
                int p = 0;
                while (Server.UdpClient != null)
                {
                    p++;
                    Server.Ping(p);
                    Thread.Sleep(500);
                }
            }));
            pingThread.Start();
        }

        private void ReadTcpPackets()
        {
            int opCode = Server.TcpStream.ReadByte();
            switch (opCode)
            {
                case -1:
                    return;
                case 0x10:
                {
                    int type = Server.TcpStream.ReadByte();

                    byte[] shortTemp = new byte[2];
                    Server.TcpStream.Read(shortTemp, 0, shortTemp.Length);
                    short id = BitConverter.ToInt16(shortTemp, 0);

                    int nameLength = Server.TcpStream.ReadByte();
                    byte[] nameBytes = new byte[nameLength];
                    Server.TcpStream.Read(nameBytes, 0, nameLength);
                    string name = Encoding.ASCII.GetString(nameBytes);

                    byte[] floatTemp = new byte[4];
                    Server.TcpStream.Read(floatTemp, 0, 4);
                    float x = BitConverter.ToSingle(floatTemp, 0);

                    floatTemp = new byte[4];
                    Server.TcpStream.Read(floatTemp, 0, 4);
                    float y = BitConverter.ToSingle(floatTemp, 0);

                    if (entities.ContainsKey(id))
                    {
                        //The server claims this ID has already either despawned or does not exist yet
                        //As such, I should remove and despawn any sprite that has this ID
                        Logger.Debug("Despawning " + id + " to spawn another");
                        Entity e = entities[id];
                        RemoveSprite(e);
                        entities.Remove(id);
                    }

                    if (type == 0 || type == 1)
                    {
                        var player = new NetworkPlayer(id, name) { X = x, Y = y, TintColor = type == 1 ? Players.PlayerColors[0] : Players.PlayerColors[1] };
                        AddSprite(player);
                        entities.Add(id, player);

                        var username = Text.CreateTextSprite(name, Color.White, new Font(Program.RetroFont, 18));
                        username.Y = player.Y - 32f;
                        username.X = player.X + ((username.Width - username.StringWidth) / 2f);
                        username.NeverClip = true;
                        player.Attach(username);
                        AddSprite(username);
                    }
                    else if (type == 2)
                    {
                        var bullet = new Bullet(id, name) {X = x, Y = y};
                        AddSprite(bullet);
                        entities.Add(id, bullet);
                    }
                    else
                    {
                        //TODO Or maybe check types futher
                        //TODO Spawn bullet
                    }
                }
                    break;
                case 0x06:
                {
                    int val = Server.TcpStream.ReadByte();

                    byte[] reasonLengthBytes = new byte[4];
                    Server.TcpStream.Read(reasonLengthBytes, 0, 4);
                    int length = BitConverter.ToInt32(reasonLengthBytes, 0);

                    byte[] reasonBytes = new byte[length];
                    Server.TcpStream.Read(reasonBytes, 0, length);

                    string reason = Encoding.ASCII.GetString(reasonBytes);
                    Server.matchStarted = val == 1;
                    
                    if (!Server.matchStarted)
                    {
                        if (readyText != null)
                        {
                            RemoveSprite(readyText);
                            readyText = null;
                        }

                        readyText = Text.CreateTextSprite(reason, Color.White,
                            new Font(Program.RetroFont, 18));

                        readyText.X = -Screen.Camera.X + ((readyText.Width - readyText.StringWidth) / 2f);
                        readyText.Y = 130f;
                        AddSprite(readyText);
                    }
                    else
                    {
                        RemoveSprite(readyText);
                        readyText = null;
                    }
                }
                    break;
                case 0x11:
                {
                    byte[] idBytes = new byte[2];
                    Server.TcpStream.Read(idBytes, 0, 2);
                    short id = BitConverter.ToInt16(idBytes, 0);
                    if (!entities.ContainsKey(id)) return;
                    Entity e = entities[id];
                    RemoveSprite(e);
                    entities.Remove(id);
                }
                    break;
            }
        }

        private void ReadUdpPackets()
        {
            byte[] data = Server.UdpClient.Receive(ref Server.ServerEndPoint);
            if (data[0] == 0x09) //Ping!
            {
                Server.EndPingTimer();
                Console.WriteLine("Ping: " + Server.GetLatency());
            } else if (data[0] == 0x04 && data.Length >= 30)
            {
                int packetNumber = BitConverter.ToInt32(data, 1);
                if (packetNumber < Server.lastRead)
                {
                    int dif = Server.lastRead - packetNumber;
                    if (dif >= int.MaxValue - 1000)
                    {
                        Server.lastRead = packetNumber;
                    }
                    else return;
                }
                else
                {
                    Server.lastRead = packetNumber;
                }

                short entityId = BitConverter.ToInt16(data, 5);
                float x = BitConverter.ToSingle(data, 7);
                float y = BitConverter.ToSingle(data, 11);
                float xvel = BitConverter.ToSingle(data, 15);
                float yvel = BitConverter.ToSingle(data, 19);
                bool visible = data[23] == 1;
                long serverMs = BitConverter.ToInt64(data, 24);
                bool hasTarget = data[32] == 1;
                Entity entity;
                if (entityId == 0)
                {
                    entity = player1;
                }
                else
                {
                    if (entities.ContainsKey(entityId))
                    {
                        entity = entities[entityId];
                    }
                    else return;
                }
                entity.X = x + ((Server.GetLatency()/60f)*xvel);
                entity.Y = y + ((Server.GetLatency()/60f)*yvel);

                entity.XVel = xvel;
                entity.YVel = yvel;
                if (hasTarget)
                {
                    float xTarget = BitConverter.ToSingle(data, 33);
                    float yTarget = BitConverter.ToSingle(data, 36);
                    entity.TargetX = xTarget;
                    entity.TargetY = yTarget;
                }

                entity.IsVisible = visible;
            }
        }
    }
}
