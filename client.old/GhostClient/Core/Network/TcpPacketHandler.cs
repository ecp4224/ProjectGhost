using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net.Sockets;
using System.Text;
using Ghost.Core.Network.Packets;

namespace Ghost.Core.Network
{
    public class TcpPacketHandler : PacketHandler
    {
        private static readonly Dictionary<byte, Type> packets = new Dictionary<byte, Type>()
        {
            {0x10, typeof (SpawnEntityPacket)},
            {0x06, typeof (MatchStatusPacket)},
            {0x07, typeof (MatchEndPacket)},
            {0x11, typeof (DespawnEntityPacket)},
            {0x12, typeof (PlayableUpdatedPacket)},
            {0x19, typeof (PingPacket)},
            {0x30, typeof (SpawnEffectPacket)},
            {0x31, typeof (StatsUpdatePacket)},
            {0x32, typeof (ItemActivatedPacket)},
            {0x33, typeof (ItemDeactivatedPacket)},
            {0x35, typeof(MapSettingsPacket)}
        };

        private readonly TcpClient _client;
        public TcpPacketHandler(TcpClient client)
        {
            this._client = client;
        }
 
        public override Dictionary<byte, Type> Packets
        {
            get { return packets; }
        }

        public override void Start()
        {
            Stream stream = _client.GetStream();
            while (_client.Connected)
            {
                try
                {
                    int b = stream.ReadByte();
                    if (b == -1)
                        break;

                    Handle((byte) b, stream);
                }
                catch (Exception e)
                {
                    Console.WriteLine(e);
                    Console.WriteLine("Error reading TCP Packet!");
                }
            }

            //TODO Handle disconnect!
        }
    }
}
