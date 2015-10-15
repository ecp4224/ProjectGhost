using System;
using System.Drawing;
using System.Runtime.InteropServices;
using MapCreator.GUI;
using OpenTK;

namespace MapCreator.Render
{
    [Serializable]
    [StructLayout(LayoutKind.Sequential)]
    public struct Vertex
    {
        public Vector2 position;    
        public Vector2 texture;

        public Vertex(float x, float y, float u, float v)
        {
            //position = new Vector2(x / Game.Width * 2f - 1f, y / Game.Height * 2f - 1f);
            //position = new Vector2(2 * x / Game.Height, 2 * y / Game.Height);
            position = new Vector2(x, y);
            texture = new Vector2(u, v);
        }
    }
}
