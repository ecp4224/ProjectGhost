using System;
using System.Collections.Generic;
using System.Linq;
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

        public GraphicsDeviceManager Graphics { get; private set; }
        private SpriteBatch spriteBatch;

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

            base.Initialize();
        }

        /// <summary>
        /// LoadContent will be called once per game and is the place to load
        /// all of your content.
        /// </summary>
        protected override void LoadContent()
        {
            spriteBatch = new SpriteBatch(GraphicsDevice);
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

            base.Update(gameTime);
        }

        /// <summary>
        /// This is called when the game should draw itself.
        /// </summary>
        /// <param name="gameTime">Provides a snapshot of timing values.</param>
        protected override void Draw(GameTime gameTime)
        {
            GraphicsDevice.Clear(Color.CornflowerBlue);

            _spritesLooping = true;

            spriteBatch.Begin();
            foreach (Sprite s in _sprites)
            {
                spriteBatch.Draw(s.Texture, s.Position, null, s.TexCoords, s.Origin, s.Rotation, s.Scale, s.TintColor, SpriteEffects.None, s.Layer);
            }
            spriteBatch.End();

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
        }

        public void RemoveSprite(Sprite sprite)
        {
            if (_spritesLooping)
                _spritesRemove.Add(sprite);
            else
                _sprites.Remove(sprite);
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
