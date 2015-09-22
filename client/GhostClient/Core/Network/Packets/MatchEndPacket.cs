using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using GhostClient.Core;

namespace Ghost.Core.Network.Packets
{
    public class MatchEndPacket : Packet
    {
        public MatchEndPacket(Stream stream) : base(stream)
        {
        }

        public MatchEndPacket(Stream stream, bool autoClose) : base(stream, autoClose)
        {
        }

        public MatchEndPacket(byte[] data) : base(data)
        {
        }

        protected override void OnHandlePacket()
        {
            bool isWinner = Consume(1).BoolValue;
            long matchId = Consume(8).LongValue;

            GameHandler.Game.EndMatch();
        }
    }
}
