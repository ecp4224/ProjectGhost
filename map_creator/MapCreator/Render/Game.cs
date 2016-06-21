
using System.Drawing;
using System.Linq;
using System.Windows.Forms;
using MapCreator.App;
using MapCreator.Render.Sprite;
using OpenTK.Graphics.OpenGL;

namespace MapCreator.Render
{
    public sealed class Game
    {
        public static float Width { get; private set; }
        public static float Height { get; private set; }

        private ListBox _spriteList;

        private ShaderProgram _program;

        public Map Map { get; private set; }

        public Border Border;
        public MapObject Selected;
        public Sprite.Sprite BackgroundSprite;

        public void Open(string path)
        {
            Map = Map.Create(path);

            _spriteList.Items.Clear();
            _spriteList.Items.AddRange(Map.Entities.ToArray());
            Selected = null;
        }
        public void Initialize(int width, int height)
        {
            Resize(width, height);
            
            GL.ClearColor(Color.Azure);
            GL.Enable(EnableCap.DepthTest);

            GL.Enable(EnableCap.Blend);
            GL.BlendFunc(BlendingFactorSrc.SrcAlpha, BlendingFactorDest.OneMinusSrcAlpha);

            Map = new Map();
            Border = new Border();

            _program = new ShaderProgram("shader/shader.vert", "shader/shader.frag");
        }

        public void SetControls(ListBox spriteList)
        {
            _spriteList = spriteList;
        }

        public void HandleClick(int x, int y)
        {
            foreach (var s in Map.Entities.Where(s => s.Contains(x, y)))
            {
                _spriteList.SelectedItem = s;
                break;
            }
        }

        public void Render()
        {
            GL.Clear(ClearBufferMask.ColorBufferBit | ClearBufferMask.DepthBufferBit);
            GL.ClearColor(Color.Black);

            _program.Use();

            _program.Uniform3N("uAmbientColor", Map.AmbientColor.R, Map.AmbientColor.G, Map.AmbientColor.B);
            _program.Uniform1("uAmbientPower", Map.AmbientPower);

            Map.Entities.ForEach(s => s.Render(_program));

            if (Selected != null) { Border.Render(_program); }
            if (BackgroundSprite != null) { BackgroundSprite.Render(_program); }
        }

        public void Resize(int width, int height)
        {
            
            Width = width;
            Height = height;
            
            GL.Viewport(0, 0, width, height);
        }

        public void AddSprite(MapObject sprite)
        {
            Map.Entities.Add(sprite);
        }

        public void RemoveSprite()
        {
            Map.Entities.Remove((MapObject) _spriteList.SelectedItem);
            _spriteList.Items.Remove(_spriteList.SelectedItem);
        }
    }
}
