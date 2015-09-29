﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Xna.Framework;
using Sharp2D.Core.Interfaces;

namespace Ghost.Core.Sharp2D_API
{
    public class Light : IAttachable, IMoveable2d
    {
        internal Vector3 ShaderColor;
        private Color _color;
        private float _intense;

        /// <summary>
        /// <para>This property determines how the lighting is calculated on static sprites.</para>
        /// <para>Lights cannot be moved if they are static</para>
        /// </summary>
        public bool IsStatic { get; private set; }

        public Color Color
        {
            get { return _color; }
            set
            {
                _color = value;

                ShaderColor = new Vector3((_color.R/255f), (_color.G/255f),
                    (_color.B/255f));
            }
        }

        private float x;
        private float y;

        public float X
        {
            get { return x; }
            set
            {
                if (IsStatic)
                    throw new InvalidOperationException("Static lights cannot be moved!");

                float ox = x;

                float dif = value - x;

                x = value;

                foreach (IAttachable attach in _children)
                {
                    attach.X += dif;
                }

                if (Moved != null) Moved(this, new OnMoveableMoved(this, ox, Y));
            }
        }

        public float Y
        {
            get { return y; }
            set
            {
                if (IsStatic)
                    throw new InvalidOperationException("Static lights cannot be moved!");

                float oy = y;

                float dif = value - y;

                y = value;

                foreach (IAttachable attach in _children)
                {
                    attach.Y += dif;
                }

                if (Moved != null) Moved(this, new OnMoveableMoved(this, X, oy));
            }
        }

        public float Width
        {
            get { return Radius*2; }
            set { Radius = value/2f; }
        }

        public float Height
        {
            get { return Radius*2; }
            set { Radius = value/2f; }
        }

        public event EventHandler<OnMoveableMoved> Moved;

        public Vector2 Vector2d
        {
            get { return new Vector2(X, Y); }
            set
            {
                X = value.X;
                Y = value.Y;
            }
        }

        public float Intensity
        {
            get { return _intense; }
            set
            {
                _intense = value;

                ShaderColor = new Vector3((_color.R/255f)*Intensity, (_color.G/255f)*Intensity,
                    (_color.B/255f)*Intensity);
            }
        }

        public float Radius { get; set; }

        public Light(float X, float Y, float Intensity, float Radius, Color color)
        {
            this.X = X;
            this.Y = Y;
            this.Intensity = Intensity;
            this.Radius = Radius;
            this.Color = color;
        }

        public Light(float X, float Y, float Intensity, float Radius)
            : this(X, Y, Intensity, Radius, Color.White)
        {
        }

        public Light(float X, float Y, float Intensity) : this(X, Y, Intensity, 1f)
        {
        }

        public Light(float X, float Y) : this(X, Y, 1f, 50f)
        {
        } //TODO Make good defaults for this

        public virtual void Load()
        {

        }

        public virtual void Unload()
        {

        }


        private List<IAttachable> _children = new List<IAttachable>();
        private List<IAttachable> _parents = new List<IAttachable>();

        public IList<IAttachable> Children
        {
            get { return _children; }
        }

        public IList<IAttachable> Parents
        {
            get { return _parents; }
        }

        public void Attach(IAttachable ToAttach)
        {
            if (_children.Contains(ToAttach))
                throw new ArgumentException("This attachable is already attached!");

            _children.Add(ToAttach);
            ToAttach.Parents.Add(this);
        }

        public void Deattach(IAttachable ToRemove)
        {
            if (!_children.Contains(ToRemove))
                return;

            _children.Remove(ToRemove);
            ToRemove.RemoveParent(this);
        }

        public void AddParent(IAttachable Parent)
        {
            _parents.Add(Parent);
        }

        public void RemoveParent(IAttachable Parent)
        {
            _parents.Remove(Parent);
        }
    }
}
