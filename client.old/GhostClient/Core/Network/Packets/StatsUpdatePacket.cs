using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using GhostClient.Core;

namespace Ghost.Core.Network.Packets
{
    public class StatsUpdatePacket : Packet
    {
        public StatsUpdatePacket(Stream stream) : base(stream)
        {
        }

        public StatsUpdatePacket(Stream stream, bool autoClose) : base(stream, autoClose)
        {
        }

        public StatsUpdatePacket(byte[] data) : base(data)
        {
        }

        protected override void OnHandlePacket()
        {
            string id = Consume(4).AsciiStringValue;
            double value = Consume(8).DoubleValue;

            switch (id)
            {
                case "mspd":
                    GameHandler.Game.player1.SpeedStat = value;
                    break;
                case "frte":
                    GameHandler.Game.player1.FireRateStat = value;
                    break;
            }
        }
    }
}
