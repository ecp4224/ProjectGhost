using System;
using System.Collections.Generic;
using System.Linq;
using Ghost;
using Ghost.Core;
using Ghost.Core.Handlers;
using GhostClient.Core;
using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Graphics;
using Microsoft.Xna.Framework.Input;
using Sharp2D;
using Sharp2D.Core.Interfaces;

namespace GhostClient
{
    /// <summary>
    /// This is the main type for your game
    /// </summary>
    public class Ghost : Game, ILogicContainer, ISpriteWorld
    {
        public static Ghost CurrentGhostGame;
        public static bool Replay = false;
        public static bool Fullscreen = false;

        public GraphicsDeviceManager Graphics { get; private set; }
        private SpriteBatch spriteBatch;
        private IHandler gamehandler;
        private BlendState blendState;
        private double timeSinceLastUpdate;
        private double millisecondsPerFrame = 16.6666666666667D;

        public float WidthScale { get; private set; }
        public float HeightScale { get; private set; }
        private Dictionary<BlendState, List<Sprite>> renderGroups = new Dictionary<BlendState, List<Sprite>>(); 

        public Ghost()
            : base()
        {
            Graphics = new GraphicsDeviceManager(this);
            Content.RootDirectory = "Content";
        }

        /// <summary>
        /// Allows the game to perform any initialization it needs to before starting to run.
        /// This is where it can query for any required services and load any non-graphic
        /// related content.  Calling base.Initialize will enumerate through any components
        /// and initialize them as well.
        /// </summary>
        protected override void Initialize()
        {
            CurrentGhostGame = this;

            this.TargetElapsedTime = TimeSpan.FromSeconds(1f/60f);
            if (!Replay)
            {
                gamehandler = new GameHandler(this);
            }
            else
            {
                gamehandler = new ReplayHandler();
            }
            base.Initialize();
        }

        /// <summary>
        /// LoadContent will be called once per game and is the place to load
        /// all of your content.
        /// </summary>
        protected override void LoadContent()
        {
            IsMouseVisible = true;

            spriteBatch = new SpriteBatch(GraphicsDevice);
            /*blendState = new BlendState
            {
                AlphaSourceBlend = Blend.SourceAlpha,
                AlphaDestinationBlend = Blend.DestinationAlpha
            };*/

            Graphics.PreferredBackBufferWidth = 1024;
            Graphics.PreferredBackBufferHeight = 720;
            WidthScale = (float) GraphicsAdapter.DefaultAdapter.CurrentDisplayMode.Width/
                         Graphics.PreferredBackBufferWidth;
            HeightScale = (float) GraphicsAdapter.DefaultAdapter.CurrentDisplayMode.Height/
                          Graphics.PreferredBackBufferHeight;
            Graphics.IsFullScreen = Fullscreen;
            Graphics.ApplyChanges();

            gamehandler.Start();
        }

        /// <summary>
        /// UnloadContent will be called once per game and is the place to unload
        /// all content.
        /// </summary>
        protected override void UnloadContent()
        {
            // TODO: Unload any non ContentManager content here
        }

        /// <summary>
        /// Allows the game to run logic such as updating the world,
        /// checking for collisions, gathering input, and playing audio.
        /// </summary>
        /// <param name="gameTime">Provides a snapshot of timing values.</param>
        protected override void Update(GameTime gameTime)
        {
            timeSinceLastUpdate += gameTime.ElapsedGameTime.TotalMilliseconds;

            if (timeSinceLastUpdate >= millisecondsPerFrame)
            {

                timeSinceLastUpdate = 0;
                
                _logicLooping = true;

                foreach (ILogical l in _logicals)
                {
                    if (l == null)
                    {
                        continue;;
                    }
                    l.Update();
                }

                _logicLooping = false;

                _logicals.AddRange(_logicalsAdd);
                _logicals.RemoveAll(l => _logicalsRemove.Contains(l));

                _logicalsAdd.Clear();
                _logicalsRemove.Clear();

                base.Update(gameTime);

                gamehandler.Tick();
            }
        }

        /// <summary>
        /// This is called when the game should draw itself.
        /// </summary>
        /// <param name="gameTime">Provides a snapshot of timing values.</param>
        protected override void Draw(GameTime gameTime)
        {
            GraphicsDevice.Clear(Color.Black);

            _spritesLooping = true;

            lock (spritesLock)
            {
                foreach (BlendState mode in renderGroups.Keys)
                {
                    spriteBatch.Begin(SpriteSortMode.Deferred, mode);

                    foreach (Sprite s in renderGroups[mode])
                    {
                        if (!s.IsLoaded || !s.IsVisible || Math.Abs(s.Alpha) < 0.05f)
                            continue;

                        if (s.FirstRun)
                        {
                            s.Display();
                            s.FirstRun = false;
                        }

                        s.Draw(spriteBatch);
                    }

                    spriteBatch.End();
                }
            }

            _spritesLooping = false;

            try
            {
                foreach (Sprite sprite in _spritesAdd)
                {
                    if (renderGroups.ContainsKey(sprite.BlendMode))
                        renderGroups[sprite.BlendMode].Add(sprite);
                    else
                    {
                        var list = new List<Sprite> {sprite};
                        renderGroups.Add(sprite.BlendMode, list);
                    }
                }

                foreach (Sprite sprite in _spritesRemove)
                {
                    if (renderGroups.ContainsKey(sprite.BlendMode))
                    {
                        renderGroups[sprite.BlendMode].Remove(sprite);
                        if (renderGroups[sprite.BlendMode].Count == 0)
                            renderGroups.Remove(sprite.BlendMode);
                    }
                }
            }
            catch
            {
            }

            _spritesAdd.Clear();
            _spritesRemove.Clear();

            base.Draw(gameTime);
        }

        private object spritesLock = new object();
        private readonly List<Sprite> _spritesAdd = new List<Sprite>();
        private readonly List<Sprite> _spritesRemove = new List<Sprite>();
        private bool _spritesLooping;

        public void AddSprite(Sprite sprite)
        {
            if (_spritesLooping)
                _spritesAdd.Add(sprite);
            else
            {
                lock (spritesLock)
                {
                    if (renderGroups.ContainsKey(sprite.BlendMode))
                        renderGroups[sprite.BlendMode].Add(sprite);
                    else
                    {
                        var list = new List<Sprite>();
                        list.Add(sprite);
                        renderGroups.Add(sprite.BlendMode, list);
                    }
                }
            }

            sprite.CurrentWorld = this;
            sprite.Load();

            var logical = sprite as ILogical;
            if (logical != null)
                AddLogical(logical);
        }

        public void RemoveSprite(Sprite sprite)
        {
            if (sprite == null)
                return;
            if (_spritesLooping)
                _spritesRemove.Add(sprite);
            else
            {
                lock (spritesLock)
                {
                    if (renderGroups.ContainsKey(sprite.BlendMode))
                    {
                        renderGroups[sprite.BlendMode].Remove(sprite);
                        if (renderGroups[sprite.BlendMode].Count == 0)
                            renderGroups.Remove(sprite.BlendMode);
                    }
                }
            }

            sprite.Dispose();
            //sprite.Unload();

            var logical = sprite as ILogical;
            if (logical != null)
                RemoveLogical(logical);
        }

        public List<Sprite> GetSprites
        {
            get
            {
                var temp = new List<Sprite>();
                foreach (var mode in renderGroups.Keys)
                {
                    temp.AddRange(renderGroups[mode]);
                }

                return temp;
            }
        }

        private readonly List<ILogical> _logicals = new List<ILogical>();
        private readonly List<ILogical> _logicalsAdd = new List<ILogical>();
        private readonly List<ILogical> _logicalsRemove = new List<ILogical>();
        private bool _logicLooping;

        public List<ILogical> LogicalList
        {
            get { return _logicals; }
        }

        public void AddLogical(ILogical logical)
        {
            if (!_logicLooping)
                _logicals.Add(logical);
            else
                _logicalsAdd.Add(logical);
        }

        public void RemoveLogical(ILogical logical)
        {
            if (!_logicLooping)
                _logicals.Remove(logical);
            else
                _logicalsRemove.Add(logical);
        }

        public ILogical AddLogical(Action action)
        {
            if (action == null)
                throw new ArgumentException("Action cannot be null!");

            var logical = new DummyLogical(action);

            if (!_logicLooping)
                _logicals.Add(logical);
            else
                _logicalsAdd.Add(logical);

            return logical;
        }
    }

    internal class DummyLogical : ILogical
    {
        readonly Action _callback;

        public DummyLogical(Action callback)
        {
            _callback = callback;
        }

        public void Update()
        {
            _callback();
        }

        public void Dispose()
        {
        }
    }
}
