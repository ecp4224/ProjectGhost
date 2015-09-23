using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;

namespace Ghost.Core.Network.Packets
{
    public class PingPacket : Packet
    {
        public PingPacket(Stream stream) : base(stream)
        {
        }

        public PingPacket(Stream stream, bool autoClose) : base(stream, autoClose)
        {
        }

        public PingPacket(byte[] data) : base(data)
        {
        }

        protected override void OnHandlePacket()
        {
            int id = Consume(4).IntValue;

            Server.EndPingTimer();
        }
    }
}
