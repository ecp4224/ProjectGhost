using System;
using System.Runtime.InteropServices;
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
            position = new Vector2(x, y);
            texture = new Vector2(u, v);
        }
    }
}
