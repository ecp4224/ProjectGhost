#region Using Statements
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
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
        static void Main(string[] args)
        {
            Server.Ip = args[0];
            Server.useWASD = args.Contains("-wasd");
            if (args.Contains("--offline"))
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

                    if (Server.WaitForOk())
                    {
                        Server.TcpClient.Close();
                        Server.TcpStream.Close();

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

                    Server.OnMatchFound(delegate
                    {
                        Server.TcpClient.Close();
                        Server.TcpStream.Close();

                        new Thread(new ThreadStart(delegate
                        {
                            using (var game = new Ghost())
                                game.Run();
                        })).Start();
                    });
                }
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
#endif
}
