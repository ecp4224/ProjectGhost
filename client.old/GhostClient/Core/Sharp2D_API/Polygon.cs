using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Xna.Framework;

namespace Ghost.Core.Sharp2D_API
{
    public class Polygon
    {
        private readonly Vector2[] _points;
        private Face[] _faces;

        public Vector2[] Points
        {
            get { return _points; }
        }

        public Face[] Faces
        {
            get { return _faces; }
        }

        public Polygon(params Vector2[] points)
        {
            _points = points;
            CreateFaces();
            CheckNormals();
        }

        private void CreateFaces()
        {
            _faces = new Face[_points.Length];
            for (int i = 0; i < _points.Length; i++)
            {
                Face f = i + 1 >= _points.Length ? new Face(_points[i], _points[0]) : new Face(_points[i], _points[i + 1]);
                _faces[i] = f;
            }
        }

        private void CheckNormals()
        {
            for (int i = 0; i < _faces.Length; i++)
            {
                Face face = _faces[i];
                Face nextFace = i + 1 >= _faces.Length ? _faces[0] : _faces[i + 1];

                float val = Vector2.Dot(face.Normal, nextFace.FaceVector);

                if (val > 0)
                {
                    face.Normal = Vector2.Negate(face.Normal);
                }

                face.Normal = Vector2.Normalize(face.Normal);
            }
        }

        public void Rotate(double radiusAdd)
        {
            for (int i = 0; i < Points.Length; i++)
            {
                Vector2 point = Points[i];
                Face face = Faces[i];
                Points[i] = point.Rotate(radiusAdd);
                face.Rotate(radiusAdd);
            }
        }

        public void Translate(float x, float y)
        {
            for (int i = 0; i < Points.Length; i++)
            {
                Vector2 point = Points[i];
                Face face = Faces[i];
                Points[i].X += x;
                Points[i].Y += y;
                face.Translate(x, y);
            }
        }
    }
}
