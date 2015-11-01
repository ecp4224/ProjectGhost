using System;
using System.ComponentModel;
using System.Drawing;
using MapCreator.App;
using Newtonsoft.Json;
using OpenTK;
using OpenTK.Graphics.OpenGL;

namespace MapCreator.Render.Sprite
{
    [DefaultProperty("Name")]
    public sealed class MapObject : Entity
    {
        [Category("Attributes"), Description("The name of the sprite. Used for identification only."), JsonIgnore]
        public string Name { get; set; }

        [Category("Dimensions"), Description("Sprite width."), JsonProperty("width")]
        public float Width { get; set; }

        [Category("Dimensions"), Description("Sprite height."), JsonProperty("height")]
        public float Height { get; set; }

        [JsonIgnore]
        internal Color Color { get; set; }

        private Texture _texture;
        private int _vboId;
        private Matrix4 _mvMatrix = Matrix4.Identity;

        public MapObject(short id, string name = "Sprite")
        {
            Id = id;
            Name = name;
            Color = Color.White;
            LoadTexture();
        }

        public MapObject()
        {
            Color = Color.White;
        }

        public void LoadTexture()
        {
            _texture = Texture.Get(Id);

            Width = _texture.Width;
            Height = _texture.Height;

            _vboId = GL.GenBuffer();
            UpdateBuffer();
        }

        public void Render(ShaderProgram program)
        {
            if (usingWidth != Width || usingHeight != Height)
                UpdateBuffer();

            float rotation = (float) ((Math.PI/180.0)*Rotation);

            _texture.Bind(program.Id);

            var t = Matrix4.CreateTranslation(X - Game.Width / 2, Y - Game.Height / 2, 0.0f);
            var r = Matrix4.CreateRotationZ(rotation);
            var l = Matrix4.CreateOrthographic(Game.Width, Game.Height, -1f, 1f);

            _mvMatrix = r * t;
            
            program.UniformMat4("pMatrix", ref l);
            program.UniformMat4("mvMatrix", ref _mvMatrix);

            program.Uniform4N("uColor", Color.R, Color.G, Color.B, Color.A);

            GL.BindBuffer(BufferTarget.ArrayBuffer, _vboId);
            GL.EnableVertexAttribArray(0);
            GL.EnableVertexAttribArray(1);

            GL.VertexAttribPointer(0, 2, VertexAttribPointerType.Float, false, 16, 0);
            GL.VertexAttribPointer(1, 2, VertexAttribPointerType.Float, false, 16, 8);
           
            GL.DrawArrays(PrimitiveType.Triangles, 0, 6);           

            GL.DisableVertexAttribArray(0);
            GL.DisableVertexAttribArray(1);
            GL.BindBuffer(BufferTarget.ArrayBuffer, 0);
        }

        private float usingWidth, usingHeight;
        private void UpdateBuffer()
        {
            var vertexData = new[]
            {                
                new Vertex( Width / 2,  Height / 2, 1.0f, 1.0f),
                new Vertex(-Width / 2,  Height / 2, 0.0f, 1.0f),
                new Vertex(-Width / 2, -Height / 2, 0.0f, 0.0f),

                new Vertex(-Width / 2, -Height / 2, 0.0f, 0.0f),
                new Vertex( Width / 2, -Height / 2, 1.0f, 0.0f),
                new Vertex( Width / 2,  Height / 2, 1.0f, 1.0f)
            };

            GL.BindBuffer(BufferTarget.ArrayBuffer, _vboId);
            GL.BufferData(BufferTarget.ArrayBuffer, (IntPtr)(vertexData.Length * 16), vertexData, BufferUsageHint.StaticDraw);

            usingWidth = Width;
            usingHeight = Height;
        }

        public bool Contains(float x, float y)
        {
            return X - Width / 2 <= x && x < X + Width / 2 && Y - Height / 2 <= y && y < Y + Height / 2;
        }

        public override string ToString()
        {
            return Name ?? "Sprite";
        }

        public void Init()
        {
            Rotation = Rotation*(180.0/Math.PI);
            LoadTexture();
        }
    }
}
