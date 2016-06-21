using System;
using System.Drawing;
using System.Windows.Forms;
using Newtonsoft.Json;
using OpenTK;
using OpenTK.Graphics.OpenGL;

namespace MapCreator.Render.Sprite
{
    public class Sprite
    {
        protected short Id { get; set; }
        protected string Name { get; set; }

        protected float Width { get; set; }
        protected float Height { get; set; }

        protected float X { get; set; }
        protected float Y { get; set; }

        protected double Rotation { get; set; }
        [JsonIgnore]
        public float RadRotation { get { return (float)(Rotation * Math.PI / 180f); } }

        [JsonIgnore]
        public Color Tint { get; set; }
        internal Color Color { get; set; }

        [JsonIgnore]
        public Texture Texture;
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

        public Sprite(string texturePath)
        {
            Tint = Color.White;
            Color = Color.White;

            Texture = Texture.Get(texturePath);

            Width = Texture.Width;
            Height = Texture.Height;

            X = Width / 2;
            Y = Height / 2;

            _vboId = GL.GenBuffer();
            UpdateBuffer();
        }

        public void LoadTexture()
        {
            Texture = Texture.Get(Id);

            if (Width == 0f && Height == 0f)
            {
                Width = Texture.Width;
                Height = Texture.Height;
            }

            _vboId = GL.GenBuffer();
            UpdateBuffer();
        }

        public void Render(ShaderProgram program)
        {
            if (usingWidth != Width || usingHeight != Height)
                UpdateBuffer();

            float rotation = (float)((Math.PI / 180.0) * Rotation);

            Texture.Bind(program.Id);

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
            var hw = Width / 2;
            var hh = Height / 2;

            var tw = Width / Texture.Width;
            var th = Height / Texture.Height;
            var vertexData = new[]
            {                
                new Vertex( hw,  hh,   tw,   th),
                new Vertex(-hw,  hh, 0.0f,   th),
                new Vertex(-hw, -hh, 0.0f, 0.0f),

                new Vertex(-hw, -hh, 0.0f, 0.0f),
                new Vertex( hw, -hh,   tw, 0.0f),
                new Vertex( hw,  hh,   tw,   th)
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

        [Flags]
        public enum Edge
        {
            None = 0,
            Top = 1,
            Right = 2,
            Bottom = 4,
            Left = 8,
            TopRight = Top | Right,
            TopLeft = Top | Left,
            BottomRight = Bottom | Right,
            BottomLeft = Bottom | Left
        }

        public Edge EdgeLocation(float x, float y, float offset)
        {
            var edge = Edge.None;

            var hw = Width / 2;
            var hh = Height / 2;

            var ox = x;
            var oy = y;
            var theta = (float) (Rotation * Math.PI / 180f);

            var m = Matrix3.CreateRotationZ((float) (Rotation * Math.PI / 180));
            var q = Quaternion.FromMatrix(m);

            var v = new Vector2(x - X, y - Y);
            var rot = Vector2.Transform(v, q);

            x = rot.X + X;
            y = rot.Y + Y;

            if (x < X + hw && x > X + hw - offset) { edge |= Edge.Right; }
            else if (x > X - hw && x < X - hw + offset) { edge |= Edge.Left; }

            if (y > Y - hh && y < Y - hh + offset) { edge |= Edge.Bottom; }
            else if (y < Y + hh && y > Y + hh - offset) { edge |= Edge.Top; }
            
            return edge;
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
