using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using GhostClient.Core;

namespace Ghost.Core.Network.Packets
{
    public class DespawnEntityPacket : Packet
    {
        public DespawnEntityPacket(Stream stream) : base(stream)
        {
        }

        public DespawnEntityPacket(Stream stream, bool autoClose) : base(stream, autoClose)
        {
        }

        public DespawnEntityPacket(byte[] data) : base(data)
        {
        }

        protected override void OnHandlePacket()
        {
            short id = Consume(2).ShortValue;

            GameHandler.Game.DespawnByID(id);
        }
    }
}
