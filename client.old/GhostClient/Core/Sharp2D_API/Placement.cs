using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Ghost.Core.Sharp2D_API
{
    [Flags]
    public enum Placement
    {
        Center = 0,
        Bottom = 1,
        Top = 2,
        Left = 4,
        Right = 8,

        CenterBottom = Center | Bottom,
        CenterTop = Center | Top,
        CenterLeft = Center | Left,
        CenterRight = Center | Right,
        BottomLeft = Bottom | Left,
        BottomRight = Bottom | Right,
        TopLeft = Top | Left,
        TopRight = Top | Right,
        CenterCenter = Center | Center
    }
}
