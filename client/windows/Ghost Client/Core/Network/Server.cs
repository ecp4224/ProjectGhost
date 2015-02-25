using System;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;
using Sharp2D;

namespace Ghost.Core.Network
{
    public class Server
    {
        public const string Ip = "127.0.0.1";
        public const int Port = 2546;
        public const string Httpurl = "http://" + Ip + ":8080";

        private static TcpClient tcpClient;
        private static Stream tcpStream;
        private static string _session;

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
                tcpStream = tcpClient.GetStream();
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

            tcpStream.Write(data, 0, data.Length);
        }

        public static bool WaitForOk()
        {
            int okPacket = tcpStream.ReadByte();
            if (okPacket == -1)
                throw new EndOfStreamException("Unexpected end of stream when reading OK Packet");

            int respose = tcpStream.ReadByte();
            if (respose == -1)
                throw new EndOfStreamException("Unexpected end of stream when reading OK Packet");

            return respose == 1;
        }
    }
}
