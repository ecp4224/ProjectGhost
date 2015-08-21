using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Ghost.Sprites;
using Ghost.Sprites.Items;
using Microsoft.Xna.Framework.Graphics.PackedVector;

namespace Ghost.Core
{
    public static class TypeableEntityCreator
    {
        private static readonly Dictionary<int, Type> TYPES = new Dictionary<int, Type>()
        {
            {2, typeof (Bullet)},
            {3, typeof (Laser)},
            {4, typeof (Circle)},
            {10, typeof (SpeedItem)},
            {11, typeof (HealthItem)},
            {12, typeof (ShieldItem)},
            {13, typeof (InvisibleItem)},
            {14, typeof (EmpItem)},
            {15, typeof (JamItem)}
        };

        public static void AddEntity<T>(int id) where T : Entity
        {
            if (TYPES.ContainsKey(id))
                throw new Exception("ID Already in use!");

            TYPES.Add(id, typeof(T));
        }

        public static Entity CreateEntity(int type, short id, float x, float y)
        {
            if (!TYPES.ContainsKey(type))
                return null;

            Type t = TYPES[type];
            var entity = (Entity) Activator.CreateInstance(t, id);
            entity.X = x;
            entity.Y = y;

            return entity;
        }
    }
}
