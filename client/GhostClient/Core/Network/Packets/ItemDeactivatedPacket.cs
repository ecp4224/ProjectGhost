using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using Ghost.Sprites.Effects;
using GhostClient.Core;

namespace Ghost.Core.Network.Packets
{
    public class ItemDeactivatedPacket : Packet
    {
        public ItemDeactivatedPacket(Stream stream) : base(stream)
        {
        }

        public ItemDeactivatedPacket(Stream stream, bool autoClose) : base(stream, autoClose)
        {
        }

        public ItemDeactivatedPacket(byte[] data) : base(data)
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
                    e.Orbits[0].End();
                }
            }
            Console.WriteLine("Item " + id + " was deactivated!");
        }
    }
}
