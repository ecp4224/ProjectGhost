using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading;
using Ghost;
using Ghost.Core.Network;
using Sharp2D;

namespace GhostClient.Core
{
    public class GameHandler
    {
        public static readonly Color[] PlayerColors =
        {
            Color.FromArgb(255, 197, 0, 0),
            Color.FromArgb(255, 0, 81, 197),
            Color.FromArgb(255, 0, 159, 0),
            Color.FromArgb(255, 1, 216, 0)
        };
        private Dictionary<short, Entity> entities = new Dictionary<short, Entity>();
        private Thread tcpThread, udpThread, pingThread;
        public static Sprite readyText;
        public InputEntity player1;

        public ISpriteWorld World { get; private set; }

        public GameHandler(ISpriteWorld world)
        {
            this.World = world;
            CreatePacketThreads();
        }

        private void CreatePacketThreads()
        {
            tcpThread = new Thread(new ThreadStart(delegate
            {
                Server.TcpStream.ReadTimeout = Timeout.Infinite;
                while (Server.isInMatch)
                {
                    try
                    {
                        ReadTcpPackets();
                    }
                    catch (Exception e)
                    {
                        Console.WriteLine(e);
                    }
                }
            }));
            udpThread = new Thread(new ThreadStart(delegate
            {
                while (Server.isInMatch)
                {
                    try
                    {
                        ReadUdpPackets();
                    }
                    catch (Exception e)
                    {
                        Console.WriteLine(e);
                    }
                }
            }));
            pingThread = new Thread(new ThreadStart(delegate
            {
                int p = 0;
                while (Server.UdpClient != null)
                {
                    p++;
                    Server.Ping(p);
                    Server.TcpPing(p);
                    Thread.Sleep(500);
                }
            }));
        }

        public void Start()
        {
            var loadingText = TextSprite.CreateText("Connecting to server...", "BigRetro");
            loadingText.X = 512F;
            loadingText.Y = 360F;
            AddSprite(loadingText);

            new Thread(new ThreadStart(delegate
            {
                Console.WriteLine("Connecting via TCP...");
                Server.ConnectToTCP();
                Console.WriteLine("Sending Session..");
                Server.SendSession();
                Console.WriteLine("Waiting for respose..");
                if (!Server.WaitForOk())
                {
                    Console.WriteLine("Bad session!");
                    return;
                }
                Console.WriteLine("Session good!");
                Console.WriteLine("Connecting via UDP");
                Server.ConnectToUDP();
                Console.WriteLine("Waiting for OK (10 second timeout)");
                if (!Server.WaitForOk(10))
                {
                    Console.WriteLine("Failed!");
                    return;
                }

                GetMatchInfo(loadingText);
            })).Start();
        }

        private void GetMatchInfo(TextSprite loadingText)
        {
            pingThread.Start();
            loadingText.Text = "Waiting for match info...";
            
            /*
            Server.JoinQueue(Server.ToJoin);
            if (Server.WaitForOk())*/
            //{

            Server.OnMatchFound(delegate(MatchInfo info)
            {
                RemoveSprite(loadingText);

                player1 = new InputEntity(0) {XVel = 0f, YVel = 0f, X = info.startX, Y = info.startY};
                AddSprite(player1);

                Server.isInMatch = true;
                Server.isReady = false;
                Server.matchStarted = false;

                tcpThread.Start();
                udpThread.Start();

                readyText = TextSprite.CreateText("Press space to ready up!", "Retro");
                readyText.X = 512F;
                readyText.Y = 590F;
                AddSprite(readyText);
            });
           
            //}
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
                            Entity e = entities[id];
                            RemoveSprite(e);
                            entities.Remove(id);
                        }

                        if (type == 0 || type == 1)
                        {
                            var player = new NetworkPlayer(id, name) { X = x, Y = y, TintColor = type == 1 ? PlayerColors[0] : PlayerColors[1] };
                            AddSprite(player);
                            entities.Add(id, player);

                            var username = TextSprite.CreateText(name, "Retro");
                            //var username = Text.CreateTextSprite(name, Color.White, new Font(Program.RetroFont, 18));
                            username.Y = player.Y - 32f;
                            username.X = player.X;
                            username.NeverClip = true;
                            player.Attach(username);
                            AddSprite(username);
                        }
                        else if (type == 2)
                        {
                            var bullet = new Bullet(id, name) { X = x, Y = y };
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
                            }

                            readyText = TextSprite.CreateText(reason, "Retro");
                            //readyText = Text.CreateTextSprite(reason, Color.White,
                            //    new Font(Program.RetroFont, 18));

                            readyText.X = (1024/2f);
                            readyText.Y = 590f;
                            AddSprite(readyText);

                            foreach (short id in entities.Keys)
                            {
                                entities[id].Pause();
                            }
                        }
                        else
                        {
                            RemoveSprite(readyText);

                            foreach (short id in entities.Keys)
                            {
                                entities[id].UnPause();
                            }
                        }
                    }
                    break;
                case 0x07:
                    {
                        bool winrar = Server.TcpStream.ReadByte() == 1;
                        byte[] matchIdBytes = new byte[8];
                        Server.TcpStream.Read(matchIdBytes, 0, 8);
                        long matchId = BitConverter.ToInt64(matchIdBytes, 0);

                        EndMatch();
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
                case 0x12:
                    {
                        byte[] idBytes = new byte[2];
                        Server.TcpStream.Read(idBytes, 0, 2);

                        short id = BitConverter.ToInt16(idBytes, 0);
                        int lifeCount = Server.TcpStream.ReadByte();
                        bool isDead = Server.TcpStream.ReadByte() == 1;
                        bool isFrozen = Server.TcpStream.ReadByte() == 1;
                        NetworkPlayer p;
                        if (id == 0)
                        {
                            p = player1;
                        }
                        else
                        {
                            if (!entities.ContainsKey(id)) return;

                            p = entities[id] as NetworkPlayer;
                        }
                        if (p != null)
                        {
                            p.Lives = (byte)lifeCount;
                            p.IsDead = isDead;
                            p.Frozen = isFrozen;
                        }
                    }
                    break;
                case 0x19:
                {
                    byte[] idByte = new byte[4];
                    Server.TcpStream.Read(idByte, 0, 4);
                    int id = BitConverter.ToInt32(idByte, 0);
                    Server.EndPingTimer();
                    break;
                }
            }
        }

        private void ReadUdpPackets()
        {
            byte[] data = Server.UdpClient.Receive(ref Server.ServerEndPoint);
            switch (data[0])
            {
                case 0x09:
                    Server.EndPingTimer();
                    break;
                case 0x04:
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

                    if (Server.GetLatency() > 0)
                    {
                        float ticksPassed = Server.GetLatency()/(1000f/60f);
                        float xadd = xvel*ticksPassed;
                        float yadd = xvel*ticksPassed;

                        x += xadd;
                        y += yadd;
                    }

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
                    if (Math.Abs(entity.X - x) < 2 && Math.Abs(entity.Y - y) < 2)
                    {
                        entity.X = x + ((Server.GetLatency()/60f)*xvel);
                        entity.Y = y + ((Server.GetLatency()/60f)*yvel);
                    }
                    else
                    {
                        entity.InterpolateTo(x, y, Server.UpdateInterval/1.3f);
                    }

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
                    break;
            }
        }

        private void AddSprite(Sprite s)
        {
            Ghost.CurrentGhostGame.AddSprite(s);
        }

        private void RemoveSprite(Sprite s)
        {
            Ghost.CurrentGhostGame.RemoveSprite(s);
        }

        private void EndMatch()
        {
            foreach (short id in entities.Keys)
            {
                foreach (var s in entities[id].Children.Cast<Sprite>())
                {
                    RemoveSprite(s);
                }
                RemoveSprite(entities[id]);
            }

            entities.Clear();

            if (readyText != null)
            {
                RemoveSprite(readyText);
            }

            Server.isInMatch = false;
            Server.isReady = false;
            Server.matchStarted = false;
            Server.lastRead = 0;
            Server.lastWrite = 0;

            Server.UdpClient.Close();
            Server.TcpStream.Close();
            Server.TcpClient.Close();
            Server.TcpClient = null;
            Server.UdpClient = null;

            tcpThread = null;
            udpThread = null;

            new Thread(new ThreadStart(delegate
            {
                //TODO Maybe wait for button press?
                Thread.Sleep(5000);
                Environment.Exit(0);
            })).Start();
        }
    }
}
