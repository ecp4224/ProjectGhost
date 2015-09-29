using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Security.Cryptography;
using System.Text;

namespace Ghost.Core.Network
{
    public abstract class PacketHandler
    {
        public abstract Dictionary<byte, Type> Packets { get; }

        public void Handle(byte b, Stream stream)
        {
            if (!Packets.ContainsKey(b))
            {
                Console.WriteLine("Invalid opcode " + b + "!");
                return;
            }

            Type packetType = Packets[b];
            if (packetType == null || !typeof(Packet).IsAssignableFrom(packetType))
                return;

            var packet = (Packet) Activator.CreateInstance(packetType, stream);


            packet.HandlePacket().End();
        }

        public void Handle(byte[] data)
        {
            Type packetType = Packets[data[0]];
            if (packetType == null || !typeof(Packet).IsAssignableFrom(packetType))
                return;

            byte[] pBytes = new byte[data.Length - 1];
            Array.Copy(data, 1, pBytes, 0, pBytes.Length);
            var packet = (Packet)Activator.CreateInstance(packetType, pBytes);


            packet.HandlePacket().End();
        }

        public abstract void Start();
    }
}
