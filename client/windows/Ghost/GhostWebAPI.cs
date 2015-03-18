using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Net.Sockets;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace Ghost
{
    public class GhostWebAPI
    {
        private const string Domain = "45.55.160.242";
        private const string Api = "http://" + Domain + ":8080/api/";
        public const int Port = 2546;

        public static string Session { get; private set; }
        public static TcpClient TcpClient { get; private set; }
        public static Stream TcpStream { get; private set; }

        public static async Task<Result<bool>> Login(string username, string password)
        {
            var cookies = new CookieContainer();
            var handler = new HttpClientHandler()
            {
                CookieContainer = cookies
            };
            using (var client = new HttpClient(handler))
            {
                client.BaseAddress = new Uri(Api);
                var content = new FormUrlEncodedContent(new[]
                {
                    new KeyValuePair<string, string>("username", username),
                    new KeyValuePair<string, string>("password", password)
                });

                var respose = await client.PostAsync("accounts/login", content);

                var resposeCookies = cookies.GetCookies(new Uri("http://" + Domain + "/"));
                if (respose.StatusCode != HttpStatusCode.Accepted) return new Result<bool>(false, "Invalid username or password!");

                Session = resposeCookies[0].Value;
                return new Result<bool>(true);
            }
        }

        public static async Task<Result<bool>> Register(string username, string password)
        {
            using (var client = new HttpClient())
            {
                client.BaseAddress = new Uri(Api);
                var content = new FormUrlEncodedContent(new[]
                {
                    new KeyValuePair<string, string>("username", username),
                    new KeyValuePair<string, string>("password", password)
                });

                var respose = await client.PostAsync("accounts/register", content);

                return respose.StatusCode != HttpStatusCode.Accepted ? new Result<bool>(true, await respose.Content.ReadAsStringAsync()) : new Result<bool>(true);
            }
        }

        public static async Task<Result<bool>> ConnectTCP()
        {
            try
            {
                TcpClient = new TcpClient();
                await TcpClient.ConnectAsync(Domain, Port + 1);
                TcpStream = TcpClient.GetStream();

                byte[] data = new byte[37];
                data[0] = 0x00;
                byte[] strBytes = Encoding.ASCII.GetBytes(Session);

                Array.Copy(strBytes, 0, data, 1, strBytes.Length);

                await TcpStream.WriteAsync(data, 0, data.Length);

                return new Result<bool>(await Ok());
            }
            catch (Exception e)
            {
                return new Result<bool>(false, "An error occured: " + e.Message);
            }
        }

        public static async Task<Result<bool>> ChangeDisplayName(string displayName)
        {
            if (displayName.Length > 255)
                throw new ArgumentException("DisplayName can't be more than 255 characters");
            try
            {
                byte[] data = new byte[2 + displayName.Length];
                data[0] = 0x14;
                data[1] = (byte) displayName.Length;
                Array.Copy(Encoding.ASCII.GetBytes(displayName), 0, data, 2, (byte) displayName.Length);

                await TcpStream.WriteAsync(data, 0, data.Length);

                return new Result<bool>(await Ok());
            }
            catch (Exception e)
            {
                return new Result<bool>(false, "An error occured: " + e.Message);
            }
        }

        public static async Task<bool> Ok(int timeout = Timeout.Infinite)
        {
            try
            {
                TcpStream.ReadTimeout = (timeout == Timeout.Infinite ? timeout : timeout * 1000);
                byte[] packet = new byte[2];
                int size = await TcpStream.ReadAsync(packet, 0, 2);
                if (size != 2)
                    throw new EndOfStreamException("Unexpected end of stream when reading OK Packet");

                return packet[1] == 1;
            }
            catch (TimeoutException e)
            {
                return false;
            }
        }
    }

    public class Result<T>
    {
        public T Value { get; private set; }
        public string Reason { get; private set; }

        public Result(T value, string reason)
        {
            this.Value = value;
            this.Reason = reason;
        }

        public Result(T value)
        {
            this.Value = value;
            this.Reason = "N/A";
        } 
    }

    public enum QueueType : byte
    {
        [Description("Face off 2 random opponent with 1 ally! A 2v2 to the death! (3 lives, unranked)")]
        Random2V2 = 1,
        [Description("1v1 fit me m8. Face off with a random opponent in a battle to the death. (3 lives, unranked)")]
        Random1V1 = 2
    }
}
