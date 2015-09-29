using System;
using System.Collections.Generic;
using System.Linq;
using System.Security.Cryptography.X509Certificates;
using System.Text;
using Ghost.Core.Sharp2D_API;

namespace Ghost.Sprites.Map
{
    public interface IPhysics
    {
        Hitbox Hitbox { get; }
    }

    public static class PhysicsObjects
    {
        public static readonly List<IPhysics> PhysicsItems = new List<IPhysics>(); 
    }
}
