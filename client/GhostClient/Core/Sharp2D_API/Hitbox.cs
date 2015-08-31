using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Xna.Framework;

namespace Ghost.Core.Sharp2D_API
{
    public class Hitbox
    {
        private Polygon _polygon;
        private string name;

        public string Name
        {
            get { return name; }
        }

        public Polygon Polygon
        {
            get { return _polygon; }
        }

        public Hitbox(string name, Polygon bounds)
        {
            _polygon = bounds;
            this.name = name;
        }

        public Hitbox(string name, params Vector2[] points)
        {
            _polygon = new Polygon(points);
            this.name = name;
        }

        public bool isPointInside(Vector2 point)
        {
            return Vector2Utils.isPointInside(point, _polygon.Points);
        }

        public bool isHitboxInside(Hitbox hitbox)
        {
            foreach (Vector2 point in hitbox.Polygon.Points)
            {
                if (Vector2Utils.isPointInside(point, Polygon.Points))
                {
                    return true;
                }
            }
            foreach (Face face in hitbox.Polygon.Faces)
            {
                foreach (Face face2 in _polygon.Faces)
                {
                    bool isIntersecting = Vector2Utils.lineIntersects(face.PointA, face.PointB, face2.PointA,
                        face2.PointB);
                    if (isIntersecting)
                        return true;
                }
            }

            return false;
        }
    }
}
