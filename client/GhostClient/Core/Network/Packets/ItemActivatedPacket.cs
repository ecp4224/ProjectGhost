using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using Ghost.Sprites.Effects;
using GhostClient.Core;

namespace Ghost.Core.Network.Packets
{
    public class ItemActivatedPacket : Packet
    {
        public ItemActivatedPacket(Stream stream) : base(stream)
        {
        }

        public ItemActivatedPacket(Stream stream, bool autoClose) : base(stream, autoClose)
        {
        }

        public ItemActivatedPacket(byte[] data) : base(data)
        {
        }

        protected override void OnHandlePacket()
        {
            short id = Consume(2).ShortValue;
            short owner = Consume(2).ShortValue;

            if (id == 12)
            {
                var e = GameHandler.Game.FindEntity(owner) as NetworkPlayer;
                if (e != null)
                {
                    var effect = new OrbitEffect(e);
                    effect.Begin();
                }
            }
            Console.WriteLine("Item " + id + " was activated!");
        }
    }
}
