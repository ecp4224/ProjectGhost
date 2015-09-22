using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using GhostClient.Core;

namespace Ghost.Core.Network.Packets
{
    public class MatchStatusPacket : Packet
    {
        public MatchStatusPacket(Stream stream) : base(stream)
        {
        }

        public MatchStatusPacket(Stream stream, bool autoClose) : base(stream, autoClose)
        {
        }

        public MatchStatusPacket(byte[] data) : base(data)
        {
        }

        protected override void OnHandlePacket()
        {
            bool state = Consume(1).BoolValue;
            int reasonLength = Consume(4).IntValue;
            string reason = Consume(reasonLength).AsciiStringValue;

            GameHandler.Game.UpdateStatus(state, reason);
        }
    }
}
