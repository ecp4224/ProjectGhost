using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using GhostClient.Core;

namespace Ghost.Core.Network.Packets
{
    public class SpawnEntityPacket : Packet
    {
        public SpawnEntityPacket(Stream stream) : base(stream)
        {
        }

        public SpawnEntityPacket(Stream stream, bool autoClose) : base(stream, autoClose)
        {
        }

        public SpawnEntityPacket(byte[] data) : base(data)
        {
        }

        protected override void OnHandlePacket()
        {
            short type = Consume(2).ShortValue;
            short id = Consume(2).ShortValue;

            byte nameLength = Consume(1).ByteValue;
            string name = Consume(nameLength).AsciiStringValue;

            float x = Consume(4).FloatValue;
            float y = Consume(4).FloatValue;

            double angle = Consume(8).DoubleValue;

            GameHandler.Game.SpawnEntity(type, id, name, x, y, angle);
        }
    }
}
