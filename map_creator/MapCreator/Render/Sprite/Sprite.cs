using System;
using System.Drawing;
using System.Windows.Forms;
using OpenTK;
using OpenTK.Graphics.OpenGL;

namespace MapCreator.Render.Sprite
{
    public class Sprite
    {
        public short Id { get; set; }
        public string Name { get; set; }

        public float Width { get; set; }
        public float Height { get; set; }

        public float X { get; set; }
        public float Y { get; set; }

        public double Rotation { get; set; }

        public Color Tint { get; set; }
        internal Color Color { get; set; }

        private Texture _texture;
        private int _vboId;
        private Matrix4 _mvMatrix = Matrix4.Identity;

        public Sprite(short id, string name = "Sprite")
        {
            Id = id;
            Name = name;
            Tint = Color.White;
            Color = Color.White;
            LoadTexture();
        }

        public Sprite()
        {
            Tint = Color.White;
            Color = Color.White;
        }

        public void LoadTexture()
        {
            _texture = Texture.Get(Id);

            if (Width == 0f && Height == 0f)
            {
                Width = _texture.Width;
                Height = _texture.Height;
            }

            _vboId = GL.GenBuffer();
            UpdateBuffer();
        }

        public void Render(ShaderProgram program)
        {
            if (usingWidth != Width || usingHeight != Height)
                UpdateBuffer();

            float rotation = (float)((Math.PI / 180.0) * Rotation);

            _texture.Bind(program.Id);

            var t = Matrix4.CreateTranslation(X - Game.Width / 2, Y - Game.Height / 2, 0.0f);
            var r = Matrix4.CreateRotationZ(rotation);
            var l = Matrix4.CreateOrthographic(Game.Width, Game.Height, -1f, 1f);

            _mvMatrix = r * t;

            program.UniformMat4("pMatrix", ref l);
            program.UniformMat4("mvMatrix", ref _mvMatrix);

            float red = (Color.R / 255f);
            float green = Color.G / 255f;
            float blue = Color.B / 255f;
            float alpha = Color.A / 255f;

            Color final = Color.FromArgb((int)(Tint.A * alpha), (int)(Tint.R * red), (int)(Tint.G * green), (int)(Tint.B * blue));


            program.Uniform4N("uColor", final.R, final.G, final.B, final.A);

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

        protected float usingWidth, usingHeight;
        protected void UpdateBuffer()
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
            Rotation = Rotation * (180.0 / Math.PI);
            LoadTexture();
        }
    }
}
