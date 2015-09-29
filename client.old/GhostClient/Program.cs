#region Using Statements
using System;
using System.Linq;
using System.Net;
using System.Threading;
using Ghost.Core;
using Ghost.Core.Network;

#endregion

namespace GhostClient
{
#if WINDOWS || LINUX
    /// <summary>
    /// The main class.
    /// </summary>
    public static class Program
    {

        /// <summary>
        /// The main entry point for the application.
        /// </summary>
        [STAThread]
        private static void Main(string[] args)
        {
            if (args.Length == 2 && args[0] == "--replay")
            {
                Console.WriteLine("Launching replay client..");
                ReplayHandler.Path = args[1];
                Ghost.Replay = true;

                using (var game = new Ghost())
                    game.Run();
            }
            else
            {
                Server.Ip = args[0];
                Server.useWASD = args.Contains("-wasd");
                Ghost.Fullscreen = args.Contains("-fullscreen");
                if (args.Contains("--offline") && args.Contains("--test"))
                {
                    Console.Write("Please specify a username to use: ");
                    string username = Console.ReadLine();

                    Console.WriteLine("Attempting to connect to offline server..");

                    if (!Server.CreateSession(username))
                    {
                        Console.WriteLine("Server is not offline!");
                        Console.WriteLine("Aborting...");
                        return;
                    }

                    Console.WriteLine("Connected!");

                    Server.ConnectToTCP();
                    if (Server.TcpStream == null)
                    {
                        Console.WriteLine("Failed to connect!");
                        return;
                    }
                    Server.SendSession();
                    if (args.Contains("--spectate"))
                    {
                        Console.WriteLine("Type match to spectate: ");
                        long id = long.Parse(Console.ReadLine());

                        if (!Server.WaitForOk())
                        {
                            Console.WriteLine("Server is not offline!");
                            Console.WriteLine("Aborting...");
                            return;
                        }

                        Server.SpectateMatch(id);
                        Server.Spectating = true;

                        if (Server.WaitForOk())
                        {
                            new Thread(new ThreadStart(delegate
                            {
                                using (var game = new Ghost())
                                    game.Run();
                            })).Start();
                        }
                        else
                        {
                            Console.WriteLine("Failed to spectate :c");
                            Console.WriteLine("Aborting..");
                            return;
                        }
                    }
                    else
                    {
                        Console.Clear();
                        Console.WriteLine("=== Queue Types ===");
                        Console.WriteLine("1 - 1v1 with guns");
                        Console.WriteLine("2 - 1v1 with lasers");
                        Console.WriteLine("3 - 1v1 choose weapon");
                        Console.WriteLine("4 - 2v2 choose weapon");
                        Console.WriteLine();
                        Console.Write("Please type the queue ID to join: ");
                        byte b = byte.Parse(Console.ReadLine());

                        if (!Server.WaitForOk())
                        {
                            Console.WriteLine("Server is not offline!");
                            Console.WriteLine("Aborting...");
                            return;
                        }

                        if (b == 3 || b == 4)
                        {
                            byte weapon = 0;
                            do
                            {
                                Console.Clear();
                                Console.WriteLine("=== Weapon Types ===");
                                Console.WriteLine("1 - Gun");
                                Console.WriteLine("2 - Laser");
                                Console.WriteLine("3 - Circle");
                                Console.WriteLine("4 - Dash");
                                Console.WriteLine("5 - Boomerang");
                                Console.WriteLine("16 - Random");
                                Console.WriteLine();
                                Console.Write("Please type the weapon ID to use: ");
                                weapon = byte.Parse(Console.ReadLine());
                            } while (weapon != 1 && weapon != 2 && weapon != 3 && weapon != 4 && weapon != 5 && weapon != 16);

                            Server.ChangeWeapon(weapon);
                        }

                        Server.JoinQueue(b);
                        if (!Server.WaitForOk())
                        {
                            Console.WriteLine("Failed to join queue!");
                            Console.WriteLine("Aborting...");
                            return;
                        }

                        Server.OnMatchFound(delegate
                        {
                            Server.TcpClient.Close();
                            Server.TcpStream.Close();

                            Thread.Sleep(1000);

                            new Thread(new ThreadStart(delegate
                            {
                                using (var game = new Ghost())
                                    game.Run();
                            })).Start();
                        });
                    }
                }
                else if (args.Contains("--offline"))
                {
                    Console.WriteLine("Attempting to connect to offline matchmaking server..");

                    Server.Port = 2178 - 1;

                    Server.Session = "11111111111111111111111111111111";
                    Server.ConnectToTCP();
                    if (Server.TcpStream == null)
                    {
                        Console.WriteLine("Failed to connect!");
                        return;
                    }
                    Server.SendSession();

                    if (!Server.WaitForOk())
                    {
                        Console.WriteLine("Server is not offline!");
                        Console.WriteLine("Aborting...");
                        return;
                    }

                    Server.WaitForNewSession();
                    Console.WriteLine("Got new session: " + Server.Session);

                    Console.Write("Please type the queue ID to join: ");
                    byte b = byte.Parse(Console.ReadLine());

                    Server.JoinQueue(b);
                    if (!Server.WaitForOk())
                    {
                        Console.WriteLine("Failed to join queue!");
                        Console.WriteLine("Aborting...");
                        return;
                    }

                    Console.WriteLine("Waiting for redirect packet..");
                    Server.OnRedirect(delegate(string ip, int port)
                    {
                        Console.WriteLine("Redirected to " + ip + ":" + port);
                        Server.Ip = ip;
                        Server.Port = port;
                        Server.ServerEndPoint = new IPEndPoint(IPAddress.Parse(ip), port);
                        
                        Server.TcpClient.Close();
                        Server.TcpClient = null;

                        using (var game = new Ghost())
                            game.Run();
                    });
                }
                else
                {
                    if (args.Length < 2)
                    {
                        Console.WriteLine("Invalid arguments!");
                        return;
                    }
                    Server.Session = args[1];

                    using (var game = new Ghost())
                        game.Run();
                }
            }
        }
    }
#endif
}
