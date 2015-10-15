using System.Collections.Generic;
using System.IO;
using System.Windows.Forms;
using OpenTK;
using OpenTK.Graphics.OpenGL;

namespace MapCreator.Render
{
    public sealed class ShaderProgram
    {
        public int Id { get; private set; }

        private Dictionary<string, int> _uniforms = new Dictionary<string, int>();

        public int Uniform(string name)
        {
            if (_uniforms.ContainsKey(name)) { return _uniforms[name]; }

            var loc = GL.GetUniformLocation(Id, name);
            _uniforms[name] = loc;

            return loc;
        }

        public ShaderProgram(string vertexPath, string fragmentPath)
        {
            Id = GL.CreateProgram();
            var vert = CompileShader(ShaderType.VertexShader, vertexPath);
            var frag = CompileShader(ShaderType.FragmentShader, fragmentPath);

            GL.AttachShader(Id, vert);
            GL.AttachShader(Id, frag);

            GL.LinkProgram(Id);
        }

        public void Use()
        {
            GL.UseProgram(Id);
        }

        private static int CompileShader(ShaderType type, string shader)
        {
            var id = GL.CreateShader(type);
            var source = File.ReadAllText(shader);

            GL.ShaderSource(id, source);
            GL.CompileShader(id);

            var log = GL.GetShaderInfoLog(id);

            if (log != "No errors.\n")
            {
                MessageBox.Show(log, "Error!");
            }
            return id;
        }

        public void UniformMat4(string name, ref Matrix4 matrix)
        {
            GL.UniformMatrix4(Uniform(name), false, ref matrix);
        }

        public void Uniform4N(string name, float x, float y, float z, float w)
        {
            GL.Uniform4(Uniform(name), x / 255.0f, y / 255.0f, z / 255.0f, w / 255.0f);
        }

        public void Uniform3N(string name, float x, float y, float z)
        {
            GL.Uniform3(Uniform(name), x / 255.0f, y / 255.0f, z / 255.0f);
        }

        public void Uniform1(string name, float x)
        {
            GL.Uniform1(Uniform(name), x);
        }
    }
}
