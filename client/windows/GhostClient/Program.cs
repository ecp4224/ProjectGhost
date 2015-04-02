#region Using Statements
using System;
using System.Collections.Generic;
using System.Linq;
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
            if (args.Length < 3)
            {
                Console.WriteLine("Invalid arguments!");
                return;
            }

            Server.Ip = args[0];
            Server.Session = args[1];
            Server.ToJoin = (QueueType)Byte.Parse(args[2]);

            Server.useWASD = args.Contains("-wasd");

            using (var game = new Ghost())
                game.Run();
        }
    }
#endif
}
