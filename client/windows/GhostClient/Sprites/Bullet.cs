﻿using System.Drawing;

namespace Ghost
{
    public class Bullet : NetworkPlayer
    {
        public Bullet(short id, string name) : base(id, name)
        {
        }

        protected override void OnLoad()
        {
            base.OnLoad();

            UniformScale = 0.25f;
            TintColor = System.Drawing.Color.FromArgb(255, 29, 53, 215);
        }

        protected override void UpdateLifeBalls()
        {
            //do nothing
        }
    }
}