using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using GhostClient.Core;

namespace Ghost.Core.Network.Packets
{
    public class SpawnEffectPacket : Packet
    {
        public SpawnEffectPacket(Stream stream) : base(stream)
        {
        }

        public SpawnEffectPacket(Stream stream, bool autoClose) : base(stream, autoClose)
        {
        }

        public SpawnEffectPacket(byte[] data) : base(data)
        {
        }

        protected override void OnHandlePacket()
        {
            byte effectType = Consume(1).ByteValue;
            int duration = Consume(4).IntValue;
            int size = Consume(4).IntValue;
            float x = Consume(4).FloatValue;
            float y = Consume(4).FloatValue;

            double rotation = Consume(8).DoubleValue;

            GameHandler.Effects[effectType].Begin(duration, size, x, y, rotation);
        }
    }
}
