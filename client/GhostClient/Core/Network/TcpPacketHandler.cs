using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net.Sockets;
using System.Text;

namespace Ghost.Core.Network
{
    public class TcpPacketHandler : PacketHandler
    {
        private static readonly Dictionary<byte, Type> packets = new Dictionary<byte, Type>();

        private TcpClient client;
        public TcpPacketHandler(TcpClient client)
        {
            this.client = client;
        }
 
        public override Dictionary<byte, Type> Packets
        {
            get { return packets; }
        }

        public override void Start()
        {
            Stream stream = client.GetStream();
            while (client.Connected)
            {
                int b = stream.ReadByte();
                if (b == -1)
                    break;

                Handle((byte)b, stream);
            }

            //TODO Handle disconnect!
        }
    }
}
