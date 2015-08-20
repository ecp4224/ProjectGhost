using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Sharp2D;
using Sharp2D.Core.Interfaces;

namespace Ghost.Sprites.Effects
{
    public interface IEffect
    {
        void Begin(int duration, int size, float x, float y, double rotation);
    }
}
