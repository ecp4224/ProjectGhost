using System;
using System.Collections.Generic;
using System.IO;
using System.IO.Compression;
using System.Linq;
using System.Text;
using Newtonsoft.Json;

namespace Ghost.Core.Network
{
    public class Packet
    {
        private Stream stream;
        private int pos = 0;
        private bool provided;
        private bool ended;

        public Packet(Stream stream)
        {
            this.stream = stream;
            this.provided = true;
        }

        public Packet(Stream stream, bool autoClose)
        {
            this.stream = stream;
            this.provided = !autoClose;
        }

        public Packet(byte[] data)
        {
            this.stream = new MemoryStream(data, false);
        }

        protected ConsumedData consume(int length)
        {
            if (ended)
                throw new InvalidOperationException("This packet has already ended!");

            byte[] data = new byte[length];
            int endPos = pos + length;
            int i = 0;
            while (pos < endPos)
            {
                int r = stream.Read(data, i, length - i);
                pos += r;
                i += r;
            }

            return new ConsumedData(data);
        }

        protected ConsumedData consume()
        {
            byte[] data = new[] {(byte) stream.ReadByte()};
            return new ConsumedData(data);
        }

        public Packet Write(byte[] val)
        {
            stream.Write(val, 0, val.Length);
            return this;
        }

        public Packet Write(byte[] val, int offset, int length)
        {
            stream.Write(val, offset, length);
            return this;
        }

        public Packet Write(byte val)
        {
            stream.WriteByte(val);
            return this;
        }

        public Packet Write(int val)
        {
            byte[] data = BitConverter.GetBytes(val);
            stream.Write(data, 0, data.Length);
            return this;
        }

        public Packet Write(float val)
        {
            byte[] data = BitConverter.GetBytes(val);
            stream.Write(data, 0, data.Length);
            return this;
        }

        public Packet Write(double val)
        {
            byte[] data = BitConverter.GetBytes(val);
            stream.Write(data, 0, data.Length);
            return this;
        }

        public Packet Write(long val)
        {
            byte[] data = BitConverter.GetBytes(val);
            stream.Write(data, 0, data.Length);
            return this;
        }

        public Packet Write(short val)
        {
            byte[] data = BitConverter.GetBytes(val);
            stream.Write(data, 0, data.Length);
            return this;
        }

        public Packet Write(string val)
        {
            byte[] data = Encoding.ASCII.GetBytes(val);
            stream.Write(data, 0, data.Length);
            return this;
        }

        public Packet Write(string val, Encoding encoding)
        {
            byte[] data = encoding.GetBytes(val);
            stream.Write(data, 0, data.Length);
            return this;
        }

        public Packet Write(bool val)
        {
            stream.WriteByte(val ? (byte)1 : (byte)0);
            return this;
        }

        public void End()
        {
            if (!provided)
            {
                stream.Close();
                stream.Dispose();
            }

            ended = true;
        }

        public virtual Packet HandlePacket()
        {
            throw new InvalidOperationException("This packet is not readable!");
            return this;
        }

        public virtual Packet WritePacket()
        {
            throw new InvalidOperationException("This packet is not writeable!");
            return this;
        }
    }

    public class ConsumedData
    {
        private byte[] consumed;

        internal ConsumedData(byte[] bytes)
        {
            this.consumed = bytes;
        }

        public int IntValue
        {
            get { return BitConverter.ToInt32(consumed, 0); }
        }

        public long LongValue
        {
            get
            {
                return BitConverter.ToInt64(consumed, 0);
            }
        }

        public float FloatValue
        {
            get
            {
                return BitConverter.ToSingle(consumed, 0);
            }
        }

        public double DoubleValue
        {
            get
            {
                return BitConverter.ToDouble(consumed, 0);
            }
        }

        public short ShortValue
        {
            get
            {
                return BitConverter.ToInt16(consumed, 0);
            }
        }

        public bool BoolValue
        {
            get { return consumed[0] == 1; }
        }

        public string AsciiStringValue
        {
            get { return Encoding.ASCII.GetString(consumed); }
        }
        
        public byte AsByte
        {
            get { return consumed[0]; }
        }

        public string GetString(Encoding encoding)
        {
            return encoding.GetString(consumed);
        }

        public TObjectType Get<TObjectType>()
        {
            int uncompressedLength = BitConverter.ToInt32(consumed, 0);
            int remain = consumed.Length - 4;
            byte[] data = new byte[remain];
            Array.Copy(consumed, 4, data, 0, remain);

            string json;
            if (uncompressedLength > 600)
            {
                using (var stream = new MemoryStream(data))
                {
                    using (var gzip = new GZipStream(stream, CompressionMode.Decompress))
                    {
                        byte[] uncompressedData = new byte[uncompressedLength];

                        int i = 0;
                        while (i < uncompressedData.Length)
                        {
                            int read = gzip.Read(uncompressedData, i, uncompressedLength - i);
                            i += read;
                        }

                        json = Encoding.ASCII.GetString(uncompressedData);
                    }
                }
            }
            else
            {
                json = Encoding.ASCII.GetString(data);
            }

            return JsonConvert.DeserializeObject<TObjectType>(json);
        }
    }
}
