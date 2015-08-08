#region Using Statements
using System;
using System.Linq;
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
                if (args.Contains("--offline"))
                    Server.Ip = args[0];

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
                            Console.WriteLine("Invalid arguments!");
                            return;
                        }
                        Server.Session = args[1];

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
                                Console.WriteLine();
                                Console.Write("Please type the weapon ID to use: ");
                                weapon = byte.Parse(Console.ReadLine());
                            } while (weapon != 1 && weapon != 2 && weapon != 3);

                            Server.ChangeWeapon(weapon);
                        }

                        using (var game = new Ghost())
                            game.Run();
                    }
                }
                else if (args.Contains("--offline"))
                {
                    Console.WriteLine("Attempting to connect to offline matchmaking server..");

                    Server.Session = "16579846516579874";
                    Server.ConnectToTCP();
                    if (Server.TcpStream == null)
                    {
                        Console.WriteLine("Failed to connect!");
                        return;
                    }
                    Server.SendSession();

                    Console.Write("Please type the queue ID to join: ");
                    byte b = byte.Parse(Console.ReadLine());


                    if (!Server.WaitForOk())
                    {
                        Console.WriteLine("Server is not offline!");
                        Console.WriteLine("Aborting...");
                        return;
                    }

                    Server.JoinQueue(b);
                    if (!Server.WaitForOk())
                    {
                        Console.WriteLine("Failed to join queue!");
                        Console.WriteLine("Aborting...");
                        return;
                    }

                    Console.WriteLine("Waiting for redirect packet..");
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
