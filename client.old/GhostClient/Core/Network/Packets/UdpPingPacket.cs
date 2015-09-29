using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;

namespace Ghost.Core.Network.Packets
{
    public class UdpPingPacket : Packet
    {
        public UdpPingPacket(Stream stream) : base(stream)
        {
        }

        public UdpPingPacket(Stream stream, bool autoClose) : base(stream, autoClose)
        {
        }

        public UdpPingPacket(byte[] data) : base(data)
        {
        }

        protected override void OnHandlePacket()
        {
            Server.EndPingTimer();
        }
    }
}
