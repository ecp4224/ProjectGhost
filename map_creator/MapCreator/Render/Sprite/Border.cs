using System.Drawing;

namespace MapCreator.Render.Sprite
{
    public class Border : Sprite
    {
        private const int Thickness = 2;
        public Border() : base(short.MinValue, "Unused")
        {
            Color = Color.Red;          
        }

        public void AdjustTo(MapObject sprite)
        {
            Width = sprite.Width + 2 * Thickness;
            Height = sprite.Height + 2 * Thickness;
            X = sprite.X;
            Y = sprite.Y;
        }
    }
}
