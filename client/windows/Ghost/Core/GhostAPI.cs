using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Net.Sockets;
using System.Security.Cryptography;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Windows.Threading;
using Newtonsoft.Json;

namespace Ghost.Core
{
    public class GhostApi
    {
        public const string Domain = "45.55.160.242";
        public const string Api = "http://" + Domain + ":8080/api/";
        public const int Port = 2546;

        public static string Session { get; private set; }
        public static string Username { get; private set; }
        public static TcpClient TcpClient { get; private set; }
        public static Stream TcpStream { get; private set; }
        public static PlayerStats CurrentPlayerStats { get; private set; }

        public static async Task<Result<bool>> Login(string username, string password)
        {
            try
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
                    if (respose.StatusCode != HttpStatusCode.Accepted)
                        return new Result<bool>(false, "Invalid username or password!");

                    Session = resposeCookies[0].Value;
                    Username = username;

                    CurrentPlayerStats = JsonConvert.DeserializeObject<PlayerStats>(await respose.Content.ReadAsStringAsync());

                    return new Result<bool>(true);
                }
            }
            catch (Exception e)
            {
                return new Result<bool>(false,
                    "Error connecting to the server. Check your internet connection and try again.");
            }
        }

        public static async Task<Result<bool>> Register(string username, string password)
        {
            try
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

                    return respose.StatusCode != HttpStatusCode.Accepted
                        ? new Result<bool>(false, await respose.Content.ReadAsStringAsync())
                        : new Result<bool>(true);
                }
            }
            catch (Exception e)
            {
                return new Result<bool>(false,
                    "Error connecting to the server. Check your internet connection and try again.");
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

        public static async Task<Result<Dictionary<string, QueueInfo[]>>> GetQueues()
        {
            var results = new Dictionary<string, QueueInfo[]>();
            try
            {
                string json;
                using (var client = new WebClient())
                {
                    json = await client.DownloadStringTaskAsync(Api + "queues");
                }

                if (json == null || string.IsNullOrWhiteSpace(json))
                    return new Result<Dictionary<string, QueueInfo[]>>(results);

                results = JsonConvert.DeserializeObject<Dictionary<string, QueueInfo[]>>(json);
                return new Result<Dictionary<string, QueueInfo[]>>(results);
            }
            catch(Exception e)
            {
                return new Result<Dictionary<string, QueueInfo[]>>(results, e.Message);
            }
        }

        public static async Task<Result<QueueInfo>> GetQueue(QueueType type)
        {
            try
            {
                string json;
                using (var client = new WebClient())
                {
                    json = await client.DownloadStringTaskAsync(Api + "queues/" + (byte)type);
                }

                if (json == null || string.IsNullOrWhiteSpace(json))
                    return new Result<QueueInfo>(null, "No info on that queue found!");

                var infos = JsonConvert.DeserializeObject<QueueInfo>(json);
                return new Result<QueueInfo>(infos);
            }
            catch(Exception e)
            {
                return new Result<QueueInfo>(null, e.Message);
            }
        }

        public static async Task<Result<PlayerStats[]>> GetPlayerStats(params int[] id)
        {
            if (id.Length == 0)
                return new Result<PlayerStats[]>(new PlayerStats[0]);

            try
            {
                string json;
                using (var client = new WebClient())
                {
                    json = await client.DownloadStringTaskAsync(Api + "accounts/stats/" + string.Join(",", id));
                }

                var toReturn = JsonConvert.DeserializeObject<PlayerStats[]>(json);
                return new Result<PlayerStats[]>(toReturn);
            }
            catch (Exception e)
            {
                return new Result<PlayerStats[]>(new PlayerStats[0], e.Message);
            }
        }

        public static async Task<Result<bool>> UpdatePlayerStats()
        {
            try
            {
                string json;
                using (var client = new WebClient())
                {
                    json = await client.DownloadStringTaskAsync(Api + "accounts/stats/" + CurrentPlayerStats.Id);
                }

                CurrentPlayerStats = JsonConvert.DeserializeObject<PlayerStats>(json);
                return new Result<bool>(true);
            }
            catch (Exception e)
            {
                return new Result<bool>(false, e.Message);
            }
        }

        public static async Task<Result<bool>> ValidateSession()
        {
            if (string.IsNullOrWhiteSpace(Session) || string.IsNullOrWhiteSpace(Username))
                return new Result<bool>(false, "Not logged in");

            try
            {
                using (var client = new HttpClient())
                {
                    client.BaseAddress = new Uri(Api);
                    var content = new StringContent(Username + "&" + Session);

                    var respose = await client.PostAsync("accounts/validate", content);

                    return respose.StatusCode != HttpStatusCode.Accepted ? new Result<bool>(false, await respose.Content.ReadAsStringAsync()) : new Result<bool>(true);
                }
            }
            catch (Exception e)
            {
                return new Result<bool>(false,
                    "Error connecting to the server.");
            }
        }

        public static async void RespondToRequest(int id, bool result)
        {
            byte[] data = new byte[6];
            data[0] = 0x17;
            Array.Copy(BitConverter.GetBytes(id), 0, data, 1, 4);
            data[5] = (byte) (result ? 1 : 0);

            await TcpStream.WriteAsync(data, 0, 6);
        }

        public static void ReadPackets(Callbacks callbacks)
        {
            while (TcpClient.Connected)
            {
                int opCode = TcpStream.ReadByte();
                if (opCode == -1)
                    break;

                switch (opCode)
                {
                    case 0x15:
                        byte[] intBytes = new byte[4];
                        TcpStream.Read(intBytes, 0, 4);

                        int id = BitConverter.ToInt32(intBytes, 0);

                        bool isRequest = TcpStream.ReadByte() == 1;

                        TcpStream.Read(intBytes, 0, 4);

                        int titleLength = BitConverter.ToInt32(intBytes, 0);

                        TcpStream.Read(intBytes, 0, 4);

                        int descriptionLength = BitConverter.ToInt32(intBytes, 0);

                        byte[] titleBytes = new byte[titleLength];
                        TcpStream.Read(titleBytes, 0, titleLength);

                        string title = Encoding.ASCII.GetString(titleBytes);

                        byte[] descriptionBytes = new byte[descriptionLength];
                        TcpStream.Read(descriptionBytes, 0, descriptionLength);

                        string description = Encoding.ASCII.GetString(descriptionBytes);

                        var request = new Notification(id, title, description, isRequest);

                        if (callbacks != null)
                        {
                            if (!callbacks.Dispatcher.CheckAccess())
                            {
                                callbacks.Dispatcher.BeginInvoke(new Action(() => callbacks.OnNewNotification(request)));
                            }
                            else
                            {
                                callbacks.OnNewNotification(request);
                            }
                        }
                        break;
                    case 0x16:
                        byte[] idBytes = new byte[4];
                        TcpStream.Read(idBytes, 0, 4);

                        int rid = BitConverter.ToInt32(idBytes, 0);

                        if (callbacks != null)
                        {
                            if (!callbacks.Dispatcher.CheckAccess())
                            {
                                callbacks.Dispatcher.BeginInvoke(new Action(() => callbacks.OnRequestRemoved(rid)));
                            }
                            else
                            {
                                callbacks.OnRequestRemoved(rid);
                            }
                        }
                        break;
                }
            }
        }
    }

    public class PlayerStats
    {
        [JsonProperty("displayname")]
        public string DisplayName { get; private set; }

        [JsonProperty("username")]
        public string Username { get; private set; }

        [JsonProperty("winHash")]
        public Dictionary<string, int> WinHash { get; private set; }
 
        [JsonProperty("loseHash")]
        public Dictionary<string, int> LoseHash { get; private set; }
 
        [JsonProperty("playersKilled")]
        public int[] PlayersKilled { get; private set; }

        [JsonProperty("shotsHit")]
        public int ShotsHit { get; private set; }
        
        [JsonProperty("shotsMissed")]
        public int ShotsMissed { get; private set; }

        [JsonProperty("id")]
        public int Id { get; private set; }

        [JsonProperty("rank")]
        public double Rank { get; private set; }

        [JsonProperty("hatTricks")]
        public int HatTricks { get; private set; }

        [JsonProperty("friends")]
        public int[] Friends { get; private set; }

        public int GamesWon
        {
            get
            {
                return WinHash.Keys.Sum(key => WinHash[key]);
            }
        }

        public int GamesLost
        {
            get { return LoseHash.Keys.Sum(key => LoseHash[key]); }
        }

        public int PlayerKillCount
        {
            get { return PlayersKilled.Length; }
        }

        public double Accuracy
        {
            get { return (double) ShotsHit/(double) (ShotsHit + ShotsMissed); }
        }

        public int FriendCount
        {
            get { return Friends.Length; }
        }
    }

    public class Callbacks
    {
        public Dispatcher Dispatcher { get; private set; }
        public Action<Notification> OnNewNotification;
        public Action<int> OnRequestRemoved;

        public Callbacks(Dispatcher dispatcher)
        {
            this.Dispatcher = dispatcher;
        }
    }

    public class Notification
    {
        public int RequestId { get; private set; }
        public string Title { get; private set; }
        public string Description { get; private set; }
        public bool IsRequest { get; private set; }

        public Notification(int id, string title, string description, bool isRequest)
        {
            RequestId = id;
            Title = title;
            Description = description;
            IsRequest = isRequest;
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

    public class QueueInfo
    {
        [JsonProperty("playersInQueue")]
        public long PlayersInQueue { get; private set; }

        [JsonProperty("playersInMatch")]
        public long PlayersInMatch { get; private set; }

        [JsonProperty("description")]
        public string Description { get; private set; }

        [JsonProperty("name")]
        public string Name { get; private set; }

        [JsonProperty("type")]
        public byte RawType { get; private set; }

        public QueueType Type
        {
            get { return (QueueType) RawType; }
        }

        public long TotalPlayers
        {
            get { return PlayersInMatch + PlayersInQueue; }
        }

        public override string ToString()
        {
            var textInfo = Thread.CurrentThread.CurrentCulture.TextInfo;
            return textInfo.ToTitleCase(Name);
        }
    }

    public enum QueueType : byte
    {
        [Description("Face off 2 random opponent with 1 ally! A 2v2 to the death! (3 lives, unranked)")]
        Random2V2 = 1,
        [Description("1v1 fit me m8. Face off with a random opponent in a battle to the death. (3 lives, unranked)")]
        Random1V1 = 2,
        [Description("1v1 fit me m8. Face off with a worthy  opponent in a battle to the death. (3 lives, ranked)")]
        Ranked1V1 = 3,
        [Description("1v1 fit me m8. Face off with a worthy opponent in a battle to the death. (3 lives, unranked)")]
        Casual1V1 = 4,
        [Description("This match is unknown")]
        Unknown = 5,
        [Description("This match is private")]
        Private = 6
    }
}
