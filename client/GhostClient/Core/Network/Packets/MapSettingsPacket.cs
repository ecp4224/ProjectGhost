using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using GhostClient.Core;
using Microsoft.Xna.Framework;

namespace Ghost.Core.Network.Packets
{
    public class MapSettingsPacket : Packet
    {
        public MapSettingsPacket(Stream stream) : base(stream)
        {
        }

        public MapSettingsPacket(Stream stream, bool autoClose) : base(stream, autoClose)
        {
        }

        public MapSettingsPacket(byte[] data) : base(data)
        {
        }

        protected override void OnHandlePacket()
        {
            float power = Consume(4).FloatValue;

            int red = Consume(4).IntValue;
            int green = Consume(4).IntValue;
            int blue = Consume(4).IntValue;

            int mapNameLength = Consume(4).IntValue;
            string mapName = Consume(mapNameLength).AsciiStringValue;

            Color color = Color.FromNonPremultiplied(red, green, blue, 255);
            GhostClient.Ghost.AmbientPower = power;
            GhostClient.Ghost.AmbientColor = color;

            GameHandler.Game.PrepareMap(mapName);
        }
    }
}
