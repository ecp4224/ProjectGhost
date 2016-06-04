using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;
using System.Linq;
using MapCreator.App;
using Newtonsoft.Json;
using OpenTK.Graphics.OpenGL;

namespace MapCreator.Render
{
    public sealed class Texture
    {
        private static readonly Dictionary<string, Texture> _cache = new Dictionary<string, Texture>();
        public static List<TextureData> IdList;
 
        private int _id;

        public int Width { get; private set; }
        public int Height { get; private set; }

        private Texture() { }

        public static void Load()
        {
            var path = "entities.json";
            IdList = JsonConvert.DeserializeObject<List<TextureData>>(File.ReadAllText(path));
        }

        //More cancer, right here. .-.
        public static Dictionary<string, string> GetAssociatedData(short id)
        {
            return IdList.First(d => d.Id == id).DefaultExtras ?? new Dictionary<string, string>();
        }

        /// <summary>
        /// Gets the texture from the specified path. Textures are catched for future use.
        /// </summary>
        /// <param name="path">The path of the texture to retrieve.</param>
        /// <returns></returns>
        public static Texture Get(string path)
        {
            return _cache.ContainsKey(path) ? _cache[path] : Load(path);
        }

        public static Texture Get(short id)
        {
            return Get(IdList.First(w => w.Id == id).Path);
        }

        private static Texture Load(string path)
        {
            var texture = new Texture { _id = GL.GenTexture() };

            GL.BindTexture(TextureTarget.Texture2D, texture._id);

            GL.TexParameter(TextureTarget.Texture2D, TextureParameterName.TextureMinFilter, (int)TextureMinFilter.Linear);
            GL.TexParameter(TextureTarget.Texture2D, TextureParameterName.TextureMagFilter, (int)TextureMagFilter.Linear);
            GL.TexParameter(TextureTarget.Texture2D, TextureParameterName.TextureWrapS, (int)TextureWrapMode.Repeat);
            GL.TexParameter(TextureTarget.Texture2D, TextureParameterName.TextureWrapT, (int)TextureWrapMode.Repeat);

            var bmp = new Bitmap(path);
            var data = bmp.LockBits(new Rectangle(0, 0, bmp.Width, bmp.Height), ImageLockMode.ReadOnly, System.Drawing.Imaging.PixelFormat.Format32bppArgb);

            GL.TexImage2D(TextureTarget.Texture2D, 0, PixelInternalFormat.Rgba, data.Width, data.Height, 0,
                OpenTK.Graphics.OpenGL.PixelFormat.Bgra, PixelType.UnsignedByte, data.Scan0);

            bmp.UnlockBits(data);

            texture.Width = data.Width;
            texture.Height = data.Height;

            GL.BindTexture(TextureTarget.Texture2D, 0);

            _cache.Add(path, texture);
            return texture;
        }

        public void Bind(int programId)
        {
            GL.ActiveTexture(TextureUnit.Texture0);
            GL.BindTexture(TextureTarget.Texture2D, _id);

            var sampler = GL.GetUniformLocation(programId, "sampler");
            GL.Uniform1(sampler, 0);
        }
    }
}
