﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Xna.Framework;

namespace Ghost.Core.Sharp2D_API
{
    public class Face
    {
        private Vector2 _pointA, _pointB, _normal;
        private Vector2 _face;

        public Vector2 PointA
        {
            get { return _pointA; }
        }

        public Vector2 PointB
        {
            get { return _pointB; }
        }

        public Vector2 Normal
        {
            get { return _normal; }
            set { _normal = value; }
        }

        public Vector2 FaceVector
        {
            get { return _face; }
        }

        public Face(Vector2 pointA, Vector2 pointB)
        {
            _pointA = pointA;
            _pointB = pointB;

            this._face = Vector2.Subtract(pointB, pointA);

            CalculateNormal();
        }

        private void CalculateNormal()
        {
            _normal = new Vector2(-(_pointB.Y - _pointA.Y), (_pointB.X - _pointA.X));
        }

        public void Rotate(double raduisAdd)
        {
            _pointA = _pointA.Rotate(raduisAdd);
            _pointB = _pointB.Rotate(raduisAdd);
            _normal = _normal.Rotate(raduisAdd);
            _face = _face.Rotate(raduisAdd);
        }

        public void Translate(float x, float y)
        {
            _pointA.X += x;
            _pointA.Y += y;
            _pointB.X += x;
            _pointB.Y += y;
            _face.X += x;
            _face.Y += y;
        }
    }
}