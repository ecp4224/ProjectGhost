using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using Ghost;
using Ghost.Core;
using Ghost.Core.Handlers;
using Ghost.Core.Sharp2D_API;
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

        public static float AmbientPower { get; set; }
        public static Color AmbientColor { get; set; }

        public GraphicsDeviceManager Graphics { get; private set; }
        private SpriteBatch spriteBatch;
        private IHandler gamehandler;
        private BlendState blendState;
        private double timeSinceLastUpdate;
        private double millisecondsPerFrame = 16.6666666666667D;

        public float WidthScale { get; private set; }
        public float HeightScale { get; private set; }
        private Dictionary<BlendState, List<Sprite>> renderGroups = new Dictionary<BlendState, List<Sprite>>(); 
        private List<Light> _lights = new List<Light>();

        //Render Targets
        private RenderTarget2D _colorMapRenderTarget;
        private RenderTarget2D _depthMapRenderTarget;
        private RenderTarget2D _normalMapRenderTarget;
        private RenderTarget2D _shadowMapRenderTarget;
        private Texture2D _shadowMapTexture;
        private Texture2D _colorMapTexture;
        private Texture2D _normalMapTexture;
        private Texture2D _depthMapTexture;

        private VertexPositionTexture[] _vertices;
        private short[] ib;
        private DynamicVertexBuffer buffer;

        private Effect _lightEffect1;
        private Effect _lightEffect2;

        public struct KillMe : IVertexType
        {
            public static readonly VertexDeclaration VD = new VertexDeclaration(new[]
            {
                new VertexElement(0, VertexElementFormat.Vector4, VertexElementUsage.Position, 0),
                new VertexElement(16, VertexElementFormat.Vector2, VertexElementUsage.TextureCoordinate, 0)
            });

            public Vector4 Position;
            public Vector2 TexCoords;

            public KillMe(Vector4 pos, Vector2 texCoords)
            {
                this.Position = pos;
                this.TexCoords = texCoords;
            }

            public VertexDeclaration VertexDeclaration
            {
                get { return KillMe.VD; }
            }
        }

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

            AmbientPower = 1f;
            AmbientColor = Color.White;

            PresentationParameters pp = GraphicsDevice.PresentationParameters;
            int width = pp.BackBufferWidth;
            int height = pp.BackBufferHeight;
            SurfaceFormat format = pp.BackBufferFormat;

            _colorMapRenderTarget = new RenderTarget2D(GraphicsDevice, width, height);
            _colorMapRenderTarget = new RenderTarget2D(GraphicsDevice, width, height);
            _depthMapRenderTarget = new RenderTarget2D(GraphicsDevice, width, height);
            _normalMapRenderTarget = new RenderTarget2D(GraphicsDevice, width, height);
            _shadowMapRenderTarget = new RenderTarget2D(GraphicsDevice, width, height);

            _lightEffect1 = Content.Load<Effect>("LightingShadow");
            _lightEffect2 = Content.Load<Effect>("LightingCombined");

            _vertices = new VertexPositionTexture[4];
            _vertices[0] = new VertexPositionTexture(new Vector3(-1, 1, 0), new Vector2(0, 0));
            _vertices[1] = new VertexPositionTexture(new Vector3(1, 1, 0), new Vector2(1, 0));
            _vertices[2] = new VertexPositionTexture(new Vector3(-1, -1, 0), new Vector2(0, 1));
            _vertices[3] = new VertexPositionTexture(new Vector3(1, -1, 0), new Vector2(1, 1));
        }

        /// <summary>
        /// LoadContent will be called once per game and is the place to load
        /// all of your content.
        /// </summary>
        protected override void LoadContent()
        {
            IsMouseVisible = true;

            spriteBatch = new SpriteBatch(GraphicsDevice);

            Graphics.PreferredBackBufferWidth = 1080;
            Graphics.PreferredBackBufferHeight = 774;


            WidthScale = (float) GraphicsAdapter.DefaultAdapter.CurrentDisplayMode.Width/
                         Graphics.PreferredBackBufferWidth;
            HeightScale = (float) GraphicsAdapter.DefaultAdapter.CurrentDisplayMode.Height/
                          Graphics.PreferredBackBufferHeight;
            Graphics.IsFullScreen = true;
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
                    try
                    {
                        if (l == null)
                        {
                            continue;
                            ;
                        }
                        l.Update();
                    }
                    catch (Exception e)
                    {
                        Console.WriteLine(e);
                    }
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
            _spritesLooping = true;
            lock (spritesLock)
            {
                SortSprites();
                _colorMapTexture = DrawColorMap();
                _depthMapTexture = DrawDepthMap();
                _normalMapTexture = DrawNormalMap();

                _shadowMapTexture = GenerateShadows();

                _lightEffect2.CurrentTechnique = _lightEffect2.Techniques["DeferredCombined"];
                _lightEffect2.Parameters["ambient"].SetValue(AmbientPower);
                _lightEffect2.Parameters["ambientColor"].SetValue(AmbientColor.ToVector4());

                _lightEffect2.Parameters["lightAmbient"].SetValue(4f);
                //_lightEffect2.Parameters["ColorMap"].SetValue(_colorMapTexture);
                _lightEffect2.Parameters["ShadingMap"].SetValue(_shadowMapTexture);

                GraphicsDevice.Textures[1] = _shadowMapTexture;
                spriteBatch.Begin(SpriteSortMode.Immediate);

                foreach (var pass in _lightEffect2.CurrentTechnique.Passes)
                {
                    pass.Apply();
                    spriteBatch.Draw(_colorMapTexture, Vector2.Zero, Color.White);
                }
                spriteBatch.End();
            }

            /*GraphicsDevice.Clear(Color.Black);

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

                        s.DrawColor(spriteBatch);
                    }

                    spriteBatch.End();
                }
            }*/

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

        private void SortSprites()
        {
            var keys = new List<BlendState>(renderGroups.Keys);
            foreach (var mode in keys)
            {
                renderGroups[mode] = renderGroups[mode].OrderBy(s => s.Layer).ToList();
            }
        }

        private Texture2D DrawColorMap()
        {
            GraphicsDevice.SetRenderTarget(_colorMapRenderTarget);
            GraphicsDevice.Clear(ClearOptions.Target | ClearOptions.DepthBuffer, Color.Black, 1, 0);

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

                    s.DrawColor(spriteBatch);
                }

                spriteBatch.End();
            }

            GraphicsDevice.SetRenderTarget(null);

            return _colorMapRenderTarget;
        }

        private static readonly Color BaseDepth = Color.FromNonPremultiplied(100, 100, 100, 255);
        private Texture2D DrawDepthMap()
        {
            GraphicsDevice.SetRenderTarget(_depthMapRenderTarget);
            GraphicsDevice.Clear(ClearOptions.Target | ClearOptions.DepthBuffer, BaseDepth, 1, 0);

            foreach (BlendState mode in renderGroups.Keys)
            {
                spriteBatch.Begin(SpriteSortMode.Deferred, mode);

                foreach (Sprite s in renderGroups[mode])
                {
                    if (s.DepthTexture == null)
                        continue;
                    if (!s.IsLoaded || !s.IsVisible || Math.Abs(s.Alpha) < 0.05f)
                        continue;

                    if (s.FirstRun)
                    {
                        s.Display();
                        s.FirstRun = false;
                    }

                    s.DrawDepth(spriteBatch);
                }

                spriteBatch.End();
            }

            GraphicsDevice.SetRenderTarget(null);

            return _depthMapRenderTarget;   
        }

        private static readonly Color BaseNormal = Color.FromNonPremultiplied(128, 128, 255, 255);
        private Texture2D DrawNormalMap()
        {
            GraphicsDevice.SetRenderTarget(_normalMapRenderTarget);
            GraphicsDevice.Clear(ClearOptions.Target | ClearOptions.DepthBuffer, BaseNormal, 1, 0);

            foreach (BlendState mode in renderGroups.Keys)
            {
                spriteBatch.Begin(SpriteSortMode.Deferred, mode);

                foreach (Sprite s in renderGroups[mode])
                {
                    if (s.NormalTexture == null)
                        continue;
                    if (!s.IsLoaded || !s.IsVisible || Math.Abs(s.Alpha) < 0.05f)
                        continue;

                    if (s.FirstRun)
                    {
                        s.Display();
                        s.FirstRun = false;
                    }

                    s.DrawNormal(spriteBatch);
                }

                spriteBatch.End();
            }

            GraphicsDevice.SetRenderTarget(null);

            return _normalMapRenderTarget;  
        }

        private Texture2D GenerateShadows()
        {
            GraphicsDevice.SetRenderTarget(_shadowMapRenderTarget);
            GraphicsDevice.Clear(ClearOptions.Target | ClearOptions.DepthBuffer, Color.Black, 1, 0);

            GraphicsDevice.BlendState = BlendState.Additive;
            foreach (var light in _lights)
            {
                _lightEffect1.CurrentTechnique = _lightEffect1.Techniques["DeferredPointLight"];
                _lightEffect1.Parameters["screenWidth"].SetValue((float)GraphicsDevice.Viewport.Width);
                _lightEffect1.Parameters["screenHeight"].SetValue((float)GraphicsDevice.Viewport.Height);
                _lightEffect1.Parameters["NormalMap"].SetValue(_normalMapTexture);
                _lightEffect1.Parameters["DepthMap"].SetValue(_depthMapTexture);
                GraphicsDevice.Textures[0] = _normalMapTexture;
                GraphicsDevice.Textures[1] = _depthMapTexture;
                _lightEffect1.Parameters["lightStrength"].SetValue(light.Intensity);
                _lightEffect1.Parameters["lightPosition"].SetValue(light.Vector2d);
                _lightEffect1.Parameters["lightColor"].SetValue(light.ShaderColor);
                _lightEffect1.Parameters["lightRadius"].SetValue(light.Radius);

                foreach (var pass in _lightEffect1.CurrentTechnique.Passes)
                {
                    pass.Apply();
                    GraphicsDevice.DrawUserPrimitives(PrimitiveType.TriangleStrip, _vertices, 0, 2);
                }
            }

            GraphicsDevice.SetRenderTarget(null);

            return _shadowMapRenderTarget;
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

        public void RemoveLight(Light light)
        {
            this._lights.Remove(light);
        }

        public void AddLight(Light light)
        {
            this._lights.Add(light);
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
