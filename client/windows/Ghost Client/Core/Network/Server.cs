using System;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using Sharp2D;

namespace Ghost.Core.Network
{
    public class Server
    {
        public const string Ip = "127.0.0.1";
        public const int Port = 2546;
        public const string Httpurl = "http://" + Ip + ":8080";
        public static IPEndPoint ServerEndPoint = new IPEndPoint(IPAddress.Parse(Ip), Port);

        private static TcpClient tcpClient;
        public static UdpClient UdpClient;
        public static Stream TcpStream;
        private static string _session;
        private static bool inQueue;
        public static bool isInMatch;
        public static int lastWrite;
        public static int lastRead;

        /// <summary>
        /// Create a new session with the provided username
        /// </summary>
        /// <param name="username">The username to start a session with</param>
        /// <returns>Whether a session was started or not</returns>
        public static async Task<bool> CreateSession(string username)
        {
            try
            {
                var cookies = new CookieContainer();
                var handler = new HttpClientHandler {CookieContainer = cookies};

                using (var client = new HttpClient(handler))
                {
                    client.BaseAddress = new Uri(Httpurl);
                    var response = await client.PostAsync("/api/register", new StringContent(username));
                    if (response.StatusCode == HttpStatusCode.Accepted)
                    {
                        foreach (var cookie in cookies.GetCookies(new Uri(Httpurl + "/api/register")).Cast<Cookie>())
                        {
                            if (cookie.Name == "session")
                            {
                                _session = cookie.Value;
                            }
                        }
                    }
                    else
                    {
                        return false;
                    }
                }

                return !String.IsNullOrWhiteSpace(_session);
            }
            catch (Exception e)
            {
                Logger.CaughtException(e);
                return false;
            }
        }

        public static void ConnectToTCP()
        {
            try
            {
                tcpClient = new TcpClient();
                tcpClient.Connect(Ip, Port + 1);
                TcpStream = tcpClient.GetStream();
            }
            catch (Exception e)
            {
                Logger.CaughtException(e);
            }
        }

        public static void SendSession()
        {
            byte[] data = new byte[37];
            data[0] = 0x00;
            byte[] strBytes = Encoding.ASCII.GetBytes(_session);

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
                TcpStream.ReadTimeout = timeout;
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
            byte[] strBytes = Encoding.ASCII.GetBytes(_session);

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
            if (!inQueue)
                return;
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

        private static int startTime;
        private static int latency;
        public static bool isReady;
        public static bool matchStarted;

        public static void Ping(int ping)
        {
            byte[] data = new byte[32];
            data[0] = 0x09;
            Array.Copy(BitConverter.GetBytes(ping), 0, data, 1, 4);

            UdpClient.Send(data, data.Length);
            startTime = Screen.TickCount;
        }

        public static void EndPingTimer()
        {
            latency = (Screen.TickCount - startTime)/2;
        }

        public static int GetLatency()
        {
            return latency;
        }

        public static void Disconnect()
        {
            if (TcpStream != null)
            {
                TcpStream.Close();
                tcpClient.Close();
            }
            if (UdpClient != null)
            {
                UdpClient.Close();
            }
        }
    }

    public enum QueueType : byte
    {
        Random = 1
    }

    public struct MatchInfo
    {
        public float startX, startY;
    }
}
