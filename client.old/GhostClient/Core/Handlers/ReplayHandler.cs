using System;
using System.Collections.Generic;
using System.IO;
using System.IO.Compression;
using System.Linq;
using System.Security.Cryptography.X509Certificates;
using System.Text;
using System.Threading;
using Ghost.Core.Handlers;
using Ghost.Core.Network;
using Ghost.Core.Sharp2D_API;
using Ghost.Sprites;
using Ghost.Sprites.Items;
using GhostClient.Core;
using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Input;
using Newtonsoft.Json;
using Sharp2D;

namespace Ghost.Core
{
    public class ReplayHandler : IHandler
    {
        public static string Path;

        private Dictionary<short, Entity> entities = new Dictionary<short, Entity>();

        public Replay ReplayData;
        private bool loaded;
        private bool paused;
        private int cursor;
        private long lastUpdate;
        public void Start()
        {
            var loadingText = TextSprite.CreateText("Loading replay data...", "BigRetro");
            loadingText.X = 512F;
            loadingText.Y = 360F;
            AddSprite(loadingText);

            new Thread(new ThreadStart(delegate
            {
                byte[] data = File.ReadAllBytes(Path);
                string json;
                using (var compressed = new MemoryStream(data))
                {
                    using (var zip = new GZipStream(compressed, CompressionMode.Decompress))
                    {
                        using (var result = new MemoryStream())
                        {
                            zip.CopyTo(result);
                            json = Encoding.ASCII.GetString(result.ToArray());
                        }
                    }
                }
                ReplayData = JsonConvert.DeserializeObject<Replay>(json);

                loaded = true;

                RemoveSprite(loadingText);
            })).Start();
        }


        public void Tick()
        {
            if (!loaded || (CheckKeyboard() || paused)) return;

            ShowUpdate();

            if (cursor + 1 < ReplayData.timeline.timeline.Length)
                cursor++;
        }

        private void ShowUpdate()
        {
            var snapshot = ReplayData.timeline.timeline[cursor];

            if (snapshot.entitySpawnSnapshots != null)
            {
                foreach (var @event in snapshot.entitySpawnSnapshots)
                {
                    if (@event.isParticle)
                    {
                        SpawnParticle(@event);
                    }
                    else
                    {
                        SpawnEntity(@event.isPlayableEntity, @event.type, @event.id, @event.name, @event.x, @event.y, @event.rotation);
                    }
                }
            }

            if (snapshot.entityDespawnSnapshots != null)
            {
                foreach (var @event in snapshot.entityDespawnSnapshots)
                {
                    if (entities.ContainsKey(@event.id))
                    {
                        RemoveSprite(entities[@event.id]);

                        entities.Remove(@event.id);
                    }
                }
            }

            if (snapshot.entitySnapshots != null)
            {
                foreach (var @event in snapshot.entitySnapshots)
                {
                    if (@event == null)
                        continue;

                    UpdateEntity(@event.id, @event.x, @event.y,
                        @event.velY, @event.velY, @event.alpha,
                        @event.rotation, @event.hasTarget,
                        new Position { x = @event.targetX, y = @event.targetY });
                }
            }

            if (snapshot.playableUpdates != null)
            {
                foreach (var @event in snapshot.playableUpdates)
                {
                    UpdatePlayable(@event.id, @event.lives, @event.isDead, @event.isFrozen);
                }
            }

            if (entities.Count != snapshot.entitySnapshots.Length)
            {
                List<short> toRemove = (from id in entities.Keys where 
                                            !(entities[id] is Wall) && !(entities[id] is Mirror) 
                                        let found = snapshot.entitySnapshots.Any(entity => entity != null && id == entity.id) where !found select id).ToList();

                foreach (short id in toRemove)
                {
                    RemoveSprite(entities[id]);

                    entities.Remove(id);
                }
            }
        }

        private void SpawnParticle(EntitySpawnEvent @event)
        {
            string[] data = @event.name.Split(':');
            int duration = int.Parse(data[0]);
            int size = int.Parse(data[1]);
            double rotation = double.Parse(data[2]);

            GameHandler.Effects[@event.type].Begin(duration, size, @event.x, @event.y, rotation);
        }

        private int updateInterval = 1;
        private bool CheckKeyboard()
        {
            var keyboard = Keyboard.GetState();

            bool returnVal = false;
            ButtonChecker.CheckAndDebounceKey(keyboard, Keys.Space, delegate
            {
                paused = !paused;
            });
            ButtonChecker.CheckAndDebounceKey(keyboard, Keys.D1, delegate
            {
                updateInterval = 1;
            });
            ButtonChecker.CheckAndDebounceKey(keyboard, Keys.D2, delegate
            {
                updateInterval = 2;
            });
            ButtonChecker.CheckAndDebounceKey(keyboard, Keys.D3, delegate
            {
                updateInterval = 4;
            });
            ButtonChecker.CheckAndDebounceKey(keyboard, Keys.D4, delegate
            {
                updateInterval = 8;
            });
            ButtonChecker.CheckAndDebounceKey(keyboard, Keys.D5, delegate
            {
                updateInterval = 16;
            });
            ButtonChecker.CheckAndDebounceKey(keyboard, Keys.D6, delegate
            {
                updateInterval = 32;
            });

            ButtonChecker.CheckAndDebounceKey(keyboard, Keys.D, delegate
            {
                cursor += updateInterval;
                returnVal = true;
            });

            ButtonChecker.CheckAndDebounceKey(keyboard, Keys.A, delegate
            {
                cursor -= updateInterval;
                returnVal = true;
            });

            if (keyboard.IsKeyDown(Keys.Left))
            {
                cursor -= updateInterval;

                returnVal = true;
            }

            if (keyboard.IsKeyDown(Keys.Right))
            {
                cursor += updateInterval;

                returnVal = true;
            }

            cursor = Math.Max(0, Math.Min(cursor, ReplayData.timeline.timeline.Length - 1));

            if (returnVal)
                ShowUpdate();

            return returnVal;
        }

        private void UpdatePlayable(short id, byte lifeCount, bool isDead, bool isFrozen)
        {
            NetworkPlayer p;
            if (!entities.ContainsKey(id)) return;

            p = entities[id] as NetworkPlayer;
            if (p == null) return;
            p.Lives = lifeCount;
            p.IsDead = isDead;
            p.Frozen = isFrozen;
        }

        private void UpdateEntity(short entityId, float x, float y, float xvel, float yvel, int alpha, double rotation, bool hasTarget, Position target)
        {
            Entity entity;
            if (entities.ContainsKey(entityId))
            {
                entity = entities[entityId];
            }
            else return;
            entity.Rotation = (float)rotation;

            entity.X = x;
            entity.Y = y;

            if (hasTarget)
            {
                entity.TargetX = target.x;
                entity.TargetY = target.y;
            }

            entity.Alpha = (alpha / 255f);
            if (entity.Alpha < 100 && entity is NetworkPlayer)
                entity.Alpha = 100;
        }

        private void SpawnEntity(bool isPlayable, int type, short id, string name, float x, float y, double rotation)
        {
            if (entities.ContainsKey(id))
            {
                //The server claims this ID has already either despawned or does not exist yet
                //As such, I should remove and despawn any sprite that has this ID
                Entity e = entities[id];
                RemoveSprite(e);
                entities.Remove(id);
            }

            if (isPlayable)
            {
                bool isTeam1 = ReplayData.team1.usernames.Contains(name);
                var player = new NetworkPlayer(id, name) { X = x, Y = y, TintColor = isTeam1 ? GameHandler.PlayerColors[0] : GameHandler.PlayerColors[1], Rotation = (float) rotation};
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
            else
            {
                var entity = TypeableEntityCreator.CreateEntity(type, id, x, y);
                if (entity == null)
                {
                    Console.WriteLine("An invalid entity ID was sent from the server!");
                    Console.WriteLine("Skipping..");
                    return;
                }
                entity.Rotation = (float) rotation;
                AddSprite(entity);
                entities.Add(id, entity);
            }
        }

        private void AddSprite(Sprite s)
        {
            GhostClient.Ghost.CurrentGhostGame.AddSprite(s);
        }

        private void RemoveSprite(Sprite s)
        {
            GhostClient.Ghost.CurrentGhostGame.RemoveSprite(s);

            foreach (Sprite child in s.Children.OfType<Sprite>())
            {
                RemoveSprite(child);
            }
        }

        public class Replay
        {
            public int id;
            public Team team1;
            public Team team2;
            public Timeline timeline;
        }

        public class Team
        {
            public string[] usernames;
            public long[] playerIds;
            public int teamNumber;
        }

        public class Timeline
        {
            public WorldSnapshot[] timeline;
        }

        public class WorldSnapshot
        {
            public long snapshotTaken;
            public EntitySnapshot[] entitySnapshots;
            public EntityDespawnEvent[] entityDespawnSnapshots;
            public EntitySpawnEvent[] entitySpawnSnapshots;
            public PlayableSnapshot[] playableUpdates;
        }

        public class PlayableSnapshot
        {
            public short id;
            public byte lives;
            public bool isDead;
            public bool isFrozen;
        }

        public class EntityDespawnEvent
        {
            public short id;
        }

        public class EntitySpawnEvent
        {
            public short id;
            public String name;
            public float x, y;
            public bool isPlayableEntity;
            public bool isTypeableEntity;
            public bool isParticle;
            public byte type;
            public double rotation;
        }

        public class EntitySnapshot
        {
            public float x, y, velX, velY, targetX, targetY;
            public int alpha;
            public double rotation;
            public bool hasTarget;
            public short id;
            public bool isPlayer;

            public string name;
            public bool isPlayableEntity;
            public bool isTypeableEntity;
            public byte type;
        }

        public class Position
        {
            public float x;
            public float y;

            public Vector2 Vector2
            {
                get
                {
                    return new Vector2(x, y);
                }
            }
        }
    }
}
