using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using GhostClient.Core;

namespace Ghost.Core.Network.Packets
{
    public class SpawnPacket : Packet
    {
        public SpawnPacket(Stream stream) : base(stream)
        {
        }

        public SpawnPacket(Stream stream, bool autoClose) : base(stream, autoClose)
        {
        }

        public SpawnPacket(byte[] data) : base(data)
        {
        }

        protected override void OnHandlePacket()
        {
            short type = Consume(2).ShortValue;
            short id = Consume(2).ShortValue;

            int nameLength = Consume(4).IntValue;
            string name = Consume(nameLength).AsciiStringValue;

            float x = Consume(4).FloatValue;
            float y = Consume(4).FloatValue;

            double angle = Consume(8).DoubleValue;

            GameHandler.Game.SpawnEntity(type, id, name, x, y, angle);
        }
    }
}
