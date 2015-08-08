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

        public GraphicsDeviceManager Graphics { get; private set; }
        private SpriteBatch spriteBatch;
        private IHandler gamehandler;
        private BlendState blendState;

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
            _logicLooping = true;

            foreach (ILogical l in _logicals)
            {
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

        /// <summary>
        /// This is called when the game should draw itself.
        /// </summary>
        /// <param name="gameTime">Provides a snapshot of timing values.</param>
        protected override void Draw(GameTime gameTime)
        {
            GraphicsDevice.Clear(Color.Black);

            _spritesLooping = true;

            spriteBatch.Begin(SpriteSortMode.Deferred, BlendState.NonPremultiplied);
            foreach (Sprite s in _sprites)
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

            _spritesLooping = false;

            _sprites.AddRange(_spritesAdd);
            _sprites.RemoveAll(s => _spritesRemove.Contains(s));

            _spritesAdd.Clear();
            _spritesRemove.Clear();

            base.Draw(gameTime);
        }

        private readonly List<Sprite> _sprites = new List<Sprite>();
        private readonly List<Sprite> _spritesAdd = new List<Sprite>();
        private readonly List<Sprite> _spritesRemove = new List<Sprite>();
        private bool _spritesLooping;

        public void AddSprite(Sprite sprite)
        {
            if (_spritesLooping)
                _spritesAdd.Add(sprite);
            else
                _sprites.Add(sprite);

            sprite.CurrentWorld = this;
            sprite.Load();

            var logical = sprite as ILogical;
            if (logical != null)
                AddLogical(logical);
        }

        public void RemoveSprite(Sprite sprite)
        {
            if (_spritesLooping)
                _spritesRemove.Add(sprite);
            else
                _sprites.Remove(sprite);

            sprite.Unload();

            var logical = sprite as ILogical;
            if (logical != null)
                RemoveLogical(logical);
        }

        public List<Sprite> GetSprites { get { return _sprites; } }

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
    }
}
