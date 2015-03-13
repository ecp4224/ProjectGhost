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
        public string Username { get; private set; }
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

        
    }
}
