using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Threading;
using Ghost;
using Ghost.Core;
using Ghost.Core.Handlers;
using Ghost.Core.Network;
using Ghost.Sprites;
using Ghost.Sprites.Effects;
using Ghost.Sprites.Items;
using Microsoft.Xna.Framework;
using Sharp2D;
using Color = System.Drawing.Color;

namespace GhostClient.Core
{
    public class GameHandler : IHandler
    {
        public static readonly Color[] PlayerColors =
        {
            Color.FromArgb(197, 0, 0),
            Color.FromArgb(0, 81, 197),
            Color.FromArgb(0, 159, 0),
            Color.FromArgb(1, 216, 0)
        };

        public static readonly IEffect[] Effects =
        {
            new ChargeEffect(),
            new LineEffect(),
            new CircleEffect()
        };

        //There will only ever be one game!
        public static GameHandler Game;

        private Dictionary<short, Entity> entities = new Dictionary<short, Entity>();
        private Thread tcpThread, udpThread, pingThread;
        public static TextSprite readyText;
        public InputEntity player1;

        private Sprite timeBarSprite;

        public ISpriteWorld World { get; private set; }

        public GameHandler(ISpriteWorld world)
        {
            this.World = world;
            CreatePacketThreads();
            Game = this;
        }

        private void CreatePacketThreads()
        {
            tcpThread = new Thread(new ThreadStart(delegate
            {
                Server.TcpStream.ReadTimeout = Timeout.Infinite;
                
                var handler = new TcpPacketHandler(Server.TcpClient);
                handler.Start();
            }));
            udpThread = new Thread(new ThreadStart(delegate
            {
                var handler = new UdpPacketHandler(Server.UdpClient, Server.TcpClient);
                handler.Start();
            }));
        }

        public void Start()
        {
            var loadingText = TextSprite.CreateText("Connecting to server...", "BigRetro");
            loadingText.X = 512F;
            loadingText.Y = 360F;
            AddSprite(loadingText);

            timeBarSprite = Sprite.FromImage("sprites/time_bar.png");
            timeBarSprite.TexCoords = new Rectangle(0, 0, 0, timeBarSprite.Texture.Height);

            timeBarSprite.Y = 710 - (timeBarSprite.Height/2f);
            timeBarSprite.X = (timeBarSprite.Width/2f) + 15;

            AddSprite(timeBarSprite);

            new Thread(new ThreadStart(delegate
            {
                if (Server.TcpClient == null || !Server.TcpClient.Connected)
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
                }
                Console.WriteLine("Connecting via UDP");
                Server.ConnectToUDP();
                Console.WriteLine("Waiting for OK (10 second timeout)");
                if (!Server.WaitForOk(10))
                {
                    Console.WriteLine("Failed!");
                    return;
                }

                if (Server.Spectating)
                {
                    Spectate(loadingText);
                }
                else
                {
                    GetMatchInfo(loadingText);
                }
            })).Start();
        }

        public void Tick()
        {

        }

        private void Spectate(TextSprite loadingText)
        {
            RemoveSprite(loadingText);

            Server.isInMatch = true;
            Server.isReady = true;
            Server.matchStarted = false;

            tcpThread.Start();
            udpThread.Start();
        }

        private void GetMatchInfo(TextSprite loadingText)
        {
            loadingText.Text = "Waiting for match info...";

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
            });
        }

        public void SpawnEntity(short type, short id, string name, float x, float y, double angle)
        {
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
                var player = new NetworkPlayer(id, name)
                {
                    X = x,
                    Y = y,
                    TintColor = type == 1 ? PlayerColors[0] : PlayerColors[1]
                };
                AddSprite(player);
                entities.Add(id, player);

                var username = TextSprite.CreateText(name, "Retro");
                username.Y = player.Y - 32f;
                username.X = player.X;
                username.NeverClip = true;
                player.Attach(username);
                AddSprite(username);
            }
            else
            {
                var entity = TypeableEntityCreator.CreateEntity(type, id, x, y);
                if (entity == null)
                {
                    Console.WriteLine("An invalid entity ID was sent from the server!");
                    Console.WriteLine("Skipping..");
                    return;
                }
                entity.Rotation = (float)angle;
                AddSprite(entity);
                entities.Add(id, entity);
            }   
        }

        public void UpdateStatus(bool val, string reason)
        {
            Server.matchStarted = val;

            if (readyText == null)
            {
                readyText = TextSprite.CreateText(reason, "Retro");
                //readyText = Text.CreateTextSprite(reason, Color.White,
                //    new Font(Program.RetroFont, 18));

                readyText.X = (1024 / 2f);
                readyText.Y = 590f;
                AddSprite(readyText);
            }
            else
            {
                readyText.Text = reason;
                readyText.X = (1024 / 2f) - (readyText.Width / 2f);
            }

            if (Server.matchStarted)
            {
                foreach (short id in entities.Keys)
                {
                    entities[id].UnPause();
                }
            }
            else
            {
                foreach (short id in entities.Keys)
                {
                    entities[id].Pause();
                }
            }   
        }

        public void DespawnByID(short id)
        {
            if (!entities.ContainsKey(id)) return;
            Entity e = entities[id];
            RemoveSprite(e);
            entities.Remove(id);
        }

        public Entity FindEntity(short id)
        {
            if (id == 0)
                return player1;
            return !entities.ContainsKey(id) ? null : entities[id];
        }

        private void AddSprite(Sprite s)
        {
            Ghost.CurrentGhostGame.AddSprite(s);
        }

        private void RemoveSprite(Sprite s)
        {
            Ghost.CurrentGhostGame.RemoveSprite(s);
        }

        public void EndMatch()
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

        public void PrepareMap(string mapName)
        {
            //TODO Do something with this name


            //Once all loaded, ready up
            Server.isReady = true;
            Server.SendReady();
        }
    }
}
