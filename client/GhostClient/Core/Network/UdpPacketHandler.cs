using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Sockets;
using System.Text;

namespace Ghost.Core.Network
{
    public class UdpPacketHandler : PacketHandler
    {
        private static readonly Dictionary<byte, Type> packets = new Dictionary<byte, Type>(); 
        public override Dictionary<byte, Type> Packets
        {
            get { return packets; }
        }

        private UdpClient client;
        private TcpClient tcp;
        public UdpPacketHandler(UdpClient client, TcpClient tcp)
        {
            this.client = client;
            this.tcp = tcp;
        }

        public override void Start()
        {
            while (tcp.Connected)
            {
                byte[] data = client.Receive(ref Server.ServerEndPoint);

                Handle(data);
            }

            //TODO Handle disconnect
        }
    }
}
