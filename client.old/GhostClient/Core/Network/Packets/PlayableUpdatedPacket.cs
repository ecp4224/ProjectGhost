using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using GhostClient.Core;

namespace Ghost.Core.Network.Packets
{
    public class PlayableUpdatedPacket : Packet
    {
        public PlayableUpdatedPacket(Stream stream) : base(stream)
        {
        }

        public PlayableUpdatedPacket(Stream stream, bool autoClose) : base(stream, autoClose)
        {
        }

        public PlayableUpdatedPacket(byte[] data) : base(data)
        {
        }

        protected override void OnHandlePacket()
        {
            short id = Consume(2).ShortValue;
            byte lifeCount = Consume(1).ByteValue;
            bool isDead = Consume(1).BoolValue;
            bool isFrozen = Consume(1).BoolValue;

            var p = GameHandler.Game.FindEntity(id) as NetworkPlayer;
            if (p == null) return;

            p.Lives = lifeCount;
            p.IsDead = isDead;
            p.Frozen = isFrozen;
        }
    }
}
