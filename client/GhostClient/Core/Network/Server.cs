using System;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.Xna.Framework;

namespace Ghost.Core.Network
{
    public class Server
    {
        public static string Ip = "127.0.0.1";
        public const float UpdateInterval = 50f;
        public const int Port = 2546;
        public static IPEndPoint ServerEndPoint = new IPEndPoint(IPAddress.Parse(Ip), Port);

        public static TcpClient TcpClient;
        public static UdpClient UdpClient;
        public static Stream TcpStream;
        public static string Session;
        public static QueueType ToJoin;
        private static bool inQueue;
        public static bool isInMatch;
        public static int lastWrite;
        public static int lastRead;
        public static bool useWASD;
        public static bool Spectating { get; set; }

        /// <summary>
        /// Create a new session with the provided username
        /// </summary>
        /// <param name="username">The username to start a session with</param>
        /// <returns>Whether a session was started or not</returns>
        public static bool CreateSession(string username)
        {
            string Httpurl = "http://" + Ip + ":8080";
            try
            {
                var cookies = new CookieContainer();
                var handler = new HttpClientHandler {CookieContainer = cookies};

                using (var client = new HttpClient(handler))
                {
                    client.BaseAddress = new Uri(Httpurl);
                    var response = client.PostAsync("/api/accounts/login", new StringContent("username=" + username + "&password=offline")).Result;
                    if (response.StatusCode == HttpStatusCode.Accepted)
                    {
                        foreach (var cookie in cookies.GetCookies(new Uri(Httpurl + "/api/accounts/login")).Cast<Cookie>().Where(cookie => cookie.Name == "session"))
                        {
                            Session = cookie.Value;
                        }
                    }
                    else
                    {
                        return false;
                    }
                }

                return !String.IsNullOrWhiteSpace(Session);
            }
            catch (Exception e)
            {
                return false;
            }
        }

        public static void ConnectToTCP()
        {
            try
            {
                TcpClient = new TcpClient();
                TcpClient.Connect(Ip, Port + 1);
                TcpStream = TcpClient.GetStream();
            }
            catch (Exception e)
            {
            }
        }

        public static void SendSession()
        {
            byte[] data = new byte[37];
            data[0] = 0x00;
            byte[] strBytes = Encoding.ASCII.GetBytes(Session);

            Array.Copy(strBytes, 0, data, 1, strBytes.Length);

            TcpStream.Write(data, 0, data.Length);
        }

        public static void SendReady()
        {
            byte[] data = { 0x03, 1 };
            TcpStream.Write(data, 0, data.Length);
            isReady = true;
        }

        public static bool WaitForOk(int timeout = Timeout.Infinite)
        {
            try
            {
                TcpStream.ReadTimeout = (timeout == Timeout.Infinite ? timeout : timeout * 1000);
                int okPacket = TcpStream.ReadByte();
                if (okPacket == -1)
                    throw new EndOfStreamException("Unexpected end of stream when reading OK Packet");

                int respose = TcpStream.ReadByte();
                if (respose == -1)
                    throw new EndOfStreamException("Unexpected end of stream when reading OK Packet");

                return respose == 1;
            }
            catch (TimeoutException e)
            {
                return false;
            }
        }

        public static void ConnectToUDP()
        {
            UdpClient = new UdpClient();
            UdpClient.Connect(Ip, Port);
            
            byte[] data = new byte[37];
            data[0] = 0x00;
            byte[] strBytes = Encoding.ASCII.GetBytes(Session);

            Array.Copy(strBytes, 0, data, 1, strBytes.Length);

            UdpClient.Send(data, data.Length);
        }

        public static void JoinQueue(QueueType type)
        {
            JoinQueue((byte)type);
        }

        public static void JoinQueue(byte type)
        {
            var data = new byte[2] { 0x05, type };
            TcpStream.Write(data, 0, 2);
            inQueue = true;
        }

        public static void OnMatchFound(Action<MatchInfo> action)
        {
            new Thread(new ThreadStart(delegate
            {
                TcpStream.ReadTimeout = Timeout.Infinite;
                int b = TcpStream.ReadByte();
                if (b == -1 || b != 0x02)
                    return;

                byte[] floatTemp = new byte[4];
                TcpStream.Read(floatTemp, 0, 4);
                float startX = BitConverter.ToSingle(floatTemp, 0);

                TcpStream.Read(floatTemp, 0, 4);
                float startY = BitConverter.ToSingle(floatTemp, 0);

                var info = new MatchInfo()
                {
                    startX = startX,
                    startY = startY
                };

                action(info);
            })).Start();
        }

        public static void MovementRequest(float targetX, float targetY)
        {
            lastWrite++;
            byte[] data = new byte[14];
            data[0] = 0x08;
            Array.Copy(BitConverter.GetBytes(lastWrite), 0, data, 1, 4);
            data[5] = 0;
            Array.Copy(BitConverter.GetBytes(targetX), 0, data, 6, 4);
            Array.Copy(BitConverter.GetBytes(targetY), 0, data, 10, 4);

            UdpClient.Send(data, data.Length);
        }

        public static void FireRequest(float targetX, float targetY)
        {
            lastWrite++;
            byte[] data = new byte[14];
            data[0] = 0x08;
            Array.Copy(BitConverter.GetBytes(lastWrite), 0, data, 1, 4);
            data[5] = 1;
            Array.Copy(BitConverter.GetBytes(targetX), 0, data, 6, 4);
            Array.Copy(BitConverter.GetBytes(targetY), 0, data, 10, 4);

            UdpClient.Send(data, data.Length);
        }

        private static int startTime;
        private static float latency;
        public static bool isReady;
        public static bool matchStarted;
        private static int tcpStartTime;
        private static bool tcpPingFinished, udpPingFinished;

        public static void Ping(int ping)
        {
            if (!udpPingFinished)
                return;

            udpPingFinished = false;
            byte[] data = new byte[32];
            data[0] = 0x09;
            Array.Copy(BitConverter.GetBytes(ping), 0, data, 1, 4);

            UdpClient.Send(data, data.Length);
            TcpStream.Write(new byte[] { 0x13, 0x00 }, 0, 2);
            startTime = Environment.TickCount;
        }

        public static void EndPingTimer()
        {
            udpPingFinished = true;
            if (udpPingFinished && tcpPingFinished)
            {
                float temp = (Environment.TickCount - startTime)/2f;
                latency = (latency + temp) / 2f;
                Console.WriteLine("Ping: " + Server.GetLatency());
            }
            else
            {
                latency = (Environment.TickCount - startTime)/2f;
            }

        }

        public static float GetLatency()
        {
            return latency;
        }

        public static void Disconnect()
        {
            if (TcpStream != null)
            {
                TcpStream.Close();
                TcpClient.Close();
            }
            if (UdpClient != null)
            {
                UdpClient.Close();
            }
        }

        public static void TcpPing(int ping)
        {
            if (!tcpPingFinished)
                return;

            tcpPingFinished = false;
            byte[] data = new byte[32];
            data[0] = 0x09;
            Array.Copy(BitConverter.GetBytes(ping), 0, data, 1, 4);

            UdpClient.Send(data, data.Length);
            TcpStream.Write(new byte[] { 0x13, 0x00 }, 0, 2);
            tcpStartTime = Environment.TickCount;
        }

        public static void EndTcpPingTimer()
        {
            tcpPingFinished = true;
            if (udpPingFinished && tcpPingFinished)
            {
                float temp = (Environment.TickCount - tcpStartTime)/2f;
                latency = (latency + temp) / 2f;
                Console.WriteLine("Ping: " + Server.GetLatency());
            }
            else
            {
                latency = (Environment.TickCount - tcpStartTime) / 2f;
            }
        }

        public static void SpectateMatch(long id)
        {
            byte[] spectatePacket = new byte[9];

            spectatePacket[0] = 0x28;
            Array.Copy(BitConverter.GetBytes(id), 0, spectatePacket, 1, 8);

            TcpStream.Write(spectatePacket, 0, 9);
        }
    }

    public enum QueueType : byte
    {
        Random2V2 = 1,
        Random1V1 = 2
    }

    public struct MatchInfo
    {
        public float startX, startY;
    }
}
