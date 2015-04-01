using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;
using System.Linq;
using System.Text;
using Microsoft.Xna.Framework.Graphics;

namespace GhostClient.Core.Sharp2D_API
{
    public static class Texture2DExt
    {
        public static Texture2D FromBitmap(Bitmap bitmap)
        {
            Texture2D tx;
            using (var ms = new MemoryStream())
            {
                bitmap.Save(ms, ImageFormat.Png);
                ms.Seek(0, SeekOrigin.Begin);
                tx = Texture2D.FromStream(Ghost.CurrentGhostGame.GraphicsDevice, ms);
            }

            return tx;
        }
    }
}
