using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Xna.Framework;

namespace Ghost.Core.Sharp2D_API
{
    public static class Vector2Utils
    {
        public static bool isPointInside(Vector2 point, params Vector2[] polygon)
        {
            int i, j;
            bool c = false;
            int nvert = polygon.Length;

            for (i = 0, j = nvert - 1; i < nvert; j = i++)
            {
                if (((polygon[i].Y > point.Y) != (polygon[j].Y > point.Y)) &&
                    (point.X <
                     (polygon[j].X - polygon[i].X)*(point.Y - polygon[i].Y)/(polygon[j].Y - polygon[i].Y) + polygon[i].X))
                    c = !c;
            }

            return c;
        }

        public static Vector2[] rotatePoints(double angle, Vector2 center, params Vector2[] points)
        {
            for (int i = 0; i < points.Length; i++)
            {
                points[i] = Vector2.Add(center, Rotate(Vector2.Subtract(points[i], center), angle));
            }

            return points;
        }

        public static bool lineIntersects(Vector2 lineAStart, Vector2 lineAEnd, Vector2 lineBStart, Vector2 lineBEnd)
        {
            Vector2 temp = new Vector2(lineBStart.X - lineAStart.X, lineBStart.Y - lineAStart.Y);
            Vector2 r = new Vector2(lineAEnd.X - lineAStart.X, lineAEnd.Y - lineAStart.Y);
            Vector2 s = new Vector2(lineBEnd.X - lineBStart.X, lineBEnd.Y - lineBStart.Y);

            float tempxr = temp.X*r.Y - temp.Y*r.X;
            float tempxs = temp.X*s.Y - temp.Y*s.X;
            float rxs = r.X*s.Y - r.Y*s.X;

            if (tempxr == 0f)
            {

                return ((lineBStart.X - lineAStart.X < 0f) != (lineBStart.X - lineAEnd.X < 0f)) ||
                       ((lineBStart.Y - lineAStart.Y < 0f) != (lineBStart.Y - lineAEnd.Y < 0f));
            }

            if (rxs == 0f)
                return false;

            float rxsr = 1f/rxs;
            float t = tempxs*rxsr;
            float u = tempxr*rxsr;

            return (t >= 0f) && (t <= 1f) && (u >= 0f) && (u <= 1f);
        }

        public static Vector2 pointOfIntersection(Vector2 lineAStart, Vector2 lineAEnd, Vector2 lineBStart,
            Vector2 lineBEnd)
        {
            Vector2 temp = new Vector2(lineBStart.X - lineAStart.X, lineBStart.Y - lineAStart.Y);
            Vector2 r = new Vector2(lineAEnd.X - lineAStart.X, lineAEnd.Y - lineAStart.Y);
            Vector2 s = new Vector2(lineBEnd.X - lineBStart.X, lineBEnd.Y - lineBStart.Y);

            float tempxr = temp.X*r.Y - temp.Y*r.X;
            float tempxs = temp.X*s.Y - temp.Y*s.X;
            float rxs = r.X*s.Y - r.Y*s.X;

            if (tempxr == 0f)
            {

                return lineAStart;
            }

            if (rxs == 0f)
                return Vector2.Zero;

            float rxsr = 1f/rxs;
            float t = tempxs*rxsr;
            float u = tempxr*rxsr;

            if (t >= 0f && t <= 1f && u >= 0f && u <= 1f)
            {
                return new Vector2(lineAStart.X + t*r.X, lineAStart.Y + t*r.Y);
            }
            return Vector2.Zero;
        }

        public static Vector2 Rotate(this Vector2 vector2, double radiusAdd)
        {
            float tempX = vector2.X;
            float tempY = vector2.Y;

            vector2.X = (float)(tempX * Math.Cos(radiusAdd) - tempY * Math.Sin(radiusAdd));
            vector2.Y = (float)(tempX * Math.Sin(radiusAdd) + tempY * Math.Cos(radiusAdd));

            return vector2;   
        }
    }
}
