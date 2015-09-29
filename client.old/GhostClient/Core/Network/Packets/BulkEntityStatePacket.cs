using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using GhostClient.Core;

namespace Ghost.Core.Network.Packets
{
    public class BulkEntityStatePacket : Packet
    {
        public BulkEntityStatePacket(Stream stream) : base(stream)
        {
        }

        public BulkEntityStatePacket(Stream stream, bool autoClose) : base(stream, autoClose)
        {
        }

        public BulkEntityStatePacket(byte[] data) : base(data)
        {
        }

        protected override void OnHandlePacket()
        {
            int packetNumber = Consume(4).IntValue;
            if (packetNumber < Server.lastRead)
            {
                int dif = Server.lastRead - packetNumber;
                if (dif >= int.MaxValue - 1000)
                {
                    Server.lastRead = packetNumber;
                }
                else return;
            }

            int bulkCount = Consume(4).IntValue;
            for (int i = 0; i < bulkCount; i++)
            {
                short entityId = Consume(2).ShortValue;
                float x = Consume(4).FloatValue;
                float y = Consume(4).FloatValue;
                float xvel = Consume(4).FloatValue;
                float yvel = Consume(4).FloatValue;
                int alpha = Consume(4).IntValue;
                double rotation = Consume(8).DoubleValue;
                long serverMs = Consume(8).LongValue;
                bool hasTarget = Consume(1).BoolValue;

                if (Server.GetLatency() > 0)
                {
                    float ticksPassed = Server.GetLatency() / (1000f / 60f);
                    float xadd = xvel * ticksPassed;
                    float yadd = xvel * ticksPassed;

                    x += xadd;
                    y += yadd;
                }

                Entity entity = GameHandler.Game.FindEntity(entityId);
                if (entity == null)
                    continue;

                entity.Rotation = (float) rotation;

                if (Math.Abs(entity.X - x) < 2 && Math.Abs(entity.Y - y) < 2)
                {
                    entity.X = x + ((Server.GetLatency() / 60f) * xvel);
                    entity.Y = y + ((Server.GetLatency() / 60f) * yvel);
                }
                else
                {
                    entity.InterpolateTo(x, y, Server.UpdateInterval / 1.3f);
                }

                entity.XVel = xvel;
                entity.YVel = yvel;

                if (hasTarget)
                {
                    float xTarget = Consume(4).FloatValue;
                    float yTarget = Consume(4).FloatValue;

                    entity.TargetX = xTarget;
                    entity.TargetY = yTarget;
                }

                entity.Alpha = (alpha / 255f);
            }
        }
    }
}
