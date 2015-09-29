using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Sockets;
using System.Text;
using Ghost.Core.Network.Packets;

namespace Ghost.Core.Network
{
    public class UdpPacketHandler : PacketHandler
    {
        private static readonly Dictionary<byte, Type> packets = new Dictionary<byte, Type>()
        {
            {0x04, typeof (BulkEntityStatePacket)},
            {0x09, typeof (UdpPingPacket)}
        };


        public override Dictionary<byte, Type> Packets
        {
            get { return packets; }
        }

        private readonly UdpClient _client;
        private readonly TcpClient _tcp;
        public UdpPacketHandler(UdpClient client, TcpClient tcp)
        {
            this._client = client;
            this._tcp = tcp;
        }

        public override void Start()
        {
            while (_tcp.Connected)
            {
                try
                {
                    byte[] data = _client.Receive(ref Server.ServerEndPoint);

                    Handle(data);
                }
                catch (Exception e)
                {
                    Console.WriteLine(e);
                    Console.WriteLine("Error reading UDP Packet!");
                }
            }

            //TODO Handle disconnect
        }
    }
}
