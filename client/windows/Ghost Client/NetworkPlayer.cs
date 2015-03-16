using System;
using System.Collections.Generic;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using Ghost.Core.Network;
using Ghost.Worlds;
using OpenTK.Input;
using Sharp2D;

namespace Ghost
{
    public class NetworkPlayer : Entity
    {
        private const byte MaxLives = 3;

        private Sprite[] lifeBalls;
        private byte lives = 3;
        private bool dead = false;
        public string Username { get; private set; }

        public byte Lives
        {
            get { return lives; }
            set
            {
                lives = value;
                UpdateLifeBalls();
            }
        }

        public override float Alpha
        {
            get { return base.Alpha; }
            set
            {
                base.Alpha = value;

                if (lifeBalls == null) return;
                foreach (Sprite s in lifeBalls.Where(s => s != null))
                {
                    s.Alpha = value;
                }
            }
        }

        public bool IsDead
        {
            get { return dead; }
            set
            {
                if (!dead && value)
                {
                    TintColor = Color.FromArgb(234, 234, 32);
                    Alpha = 0.6f;
                }
                dead = value;
            }
        }

        public NetworkPlayer(short id, string name) : base(id)
        {
            Username = name;
        }

        protected override void OnLoad()
        {
            base.OnLoad();

            Texture = Texture.NewTexture("sprites/ball.png");
            if (!Texture.Loaded)
                Texture.LoadTextureFromFile();

            Width = Texture.TextureWidth;
            Height = Texture.TextureHeight;

            NeverClip = true;
        }

        protected override void OnDisplay()
        {
            UpdateLifeBalls();
        }

        protected virtual void UpdateLifeBalls()
        {
            if (lifeBalls != null)
            {
                foreach (Sprite t in lifeBalls.Where(t => t != null))
                {
                    Children.Remove(t);
                    CurrentWorld.RemoveSprite(t);
                }
                lifeBalls = null;
            }

            lifeBalls = new Sprite[MaxLives];
            for (int i = 0; i < lives; i++)
            {
                Sprite temp = Sprite.FromImage("sprites/ball.png");
                temp.Scale = 0.2F;
                temp.NeverClip = true;
                temp.X = X - ((Width / 1.5f) / 2f);
                temp.Y = Y + 40f;
                temp.TintColor = Color.FromArgb(255, 20, 183, 52);
                temp.Alpha = Alpha;

                temp.X += (((Width / 1.5f) / (MaxLives - 1)) * i);
                lifeBalls[i] = temp;

                CurrentWorld.AddSprite(lifeBalls[i]);

                Attach(temp);
            }
        }
    }
}
