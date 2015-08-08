﻿using System;
using System.Collections.Generic;
using System.IO;
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
                string json = File.ReadAllText(Path);
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
                    SpawnEntity(@event.isPlayableEntity, @event.type, @event.id, @event.name, @event.x, @event.y);
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
                    UpdateEntity(@event.id, @event.position.x, @event.position.y,
                        @event.velocity.x, @event.velocity.y, @event.alpha,
                        @event.rotation, @event.hasTarget,
                        @event.target);
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
                List<short> toRemove = (from id in entities.Keys let found = snapshot.entitySnapshots.Any(entity => id == entity.id) where !found select id).ToList();

                foreach (short id in toRemove)
                {
                    RemoveSprite(entities[id]);

                    entities.Remove(id);
                }
            }
        }


        private long lastLeftChange;
        private long lastRightChange;
        private int updateInterval = 20;
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
                updateInterval = 20;
            });
            ButtonChecker.CheckAndDebounceKey(keyboard, Keys.D2, delegate
            {
                updateInterval = 15;
            });
            ButtonChecker.CheckAndDebounceKey(keyboard, Keys.D3, delegate
            {
                updateInterval = 10;
            });
            ButtonChecker.CheckAndDebounceKey(keyboard, Keys.D4, delegate
            {
                updateInterval = 5;
            });
            ButtonChecker.CheckAndDebounceKey(keyboard, Keys.D5, delegate
            {
                updateInterval = 2;
            });
            ButtonChecker.CheckAndDebounceKey(keyboard, Keys.D6, delegate
            {
                updateInterval = 0;
            });

            if (keyboard.IsKeyDown(Keys.Left))
            {
                if (cursor > 0 && (updateInterval == 0 || Environment.TickCount - lastLeftChange > updateInterval))
                {
                    cursor--;
                    lastLeftChange = Environment.TickCount;
                }

                ShowUpdate();

                returnVal = true;
            }

            if (keyboard.IsKeyDown(Keys.Right))
            {
                if (cursor + 1 < ReplayData.timeline.timeline.Length && (updateInterval == 0 || Environment.TickCount - lastRightChange > updateInterval))
                {
                    cursor++;
                    lastRightChange = Environment.TickCount;
                }

                ShowUpdate();

                returnVal = true;
            }

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

        private void SpawnEntity(bool isPlayable, int type, short id, string name, float x, float y)
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
                var player = new NetworkPlayer(id, name) { X = x, Y = y, TintColor = isTeam1 ? GameHandler.PlayerColors[0] : GameHandler.PlayerColors[1] };
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
            else if (type == 3)
            {
                var sprite = new Laser(id) { X = x, Y = y, Alpha = 0 };
                AddSprite(sprite);
                entities.Add(id, sprite);
            }
            else if (type == 4)
            {
                var sprite = new Circle(id) { X = x, Y = y, Alpha = 0 };
                AddSprite(sprite);
                entities.Add(id, sprite);
            }
            else if (type == 10)
            {
                var sprite = new SpeedItem(id) { X = x, Y = y };
                AddSprite(sprite);
                entities.Add(id, sprite);
            }
            else
            {
                //TODO Or maybe check types futher
                //TODO Spawn bullet
            }
        }

        private void AddSprite(Sprite s)
        {
            GhostClient.Ghost.CurrentGhostGame.AddSprite(s);
        }

        private void RemoveSprite(Sprite s)
        {
            GhostClient.Ghost.CurrentGhostGame.RemoveSprite(s);
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
            public byte type;
        }

        public class EntitySnapshot
        {
            public Position position;
            public Position velocity;
            public int alpha;
            public double rotation;
            public bool hasTarget;
            public Position target;
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
