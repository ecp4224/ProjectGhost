using System;
using System.ComponentModel;
using System.Drawing;
using System.Drawing.Text;
using System.Linq;
using System.Reflection;
using System.Runtime.InteropServices;
using System.Threading;
using System.Threading.Tasks;
using Ghost.Core.Network;
using Ghost.Worlds;
using OpenTK;
using Sharp2D;
using Sharp2D.Core;

namespace Ghost
{
    class Program
    {
        public static PrivateFontCollection FontCollection = new PrivateFontCollection();
        public static FontFamily RetroFont;
        static void Main(string[] args)
        {
            try
            {
                if (args.Length != 3)
                {
                    Console.WriteLine("Invalid arguments!");
                    return;
                }

                Server.Ip = args[0];
                Server.Session = args[1];
                Server.ToJoin = (QueueType) Byte.Parse(args[2]);

                var settings = new ScreenSettings()
                {
                    GameSize = new Size(1024, 720),
                    WindowSize = new Size(1024, 720),
                    LogicTickRate = 16
                };

                Screen.DisplayScreenAsync(settings);

                Console.WriteLine("Connecting via TCP...");
                Server.ConnectToTCP();
                Console.WriteLine("Sending Session..");
                Server.SendSession();
                Console.WriteLine("Waiting for respose..");
                if (!Server.WaitForOk())
                {
                    Console.WriteLine("Bad session!");
                    return;
                }
                Console.WriteLine("Session good!");
                Console.WriteLine("Connecting via UDP");
                Server.ConnectToUDP();
                Console.WriteLine("Waiting for OK (10 second timeout)");
                if (!Server.WaitForOk(10))
                {
                    Console.WriteLine("Failed!");
                    return;
                }

                LoadFonts();

                RetroFont = FontCollection.Families.FirstOrDefault(f => f.Name == "NBP Informa FiveSix");
                var world = new QueueWorld();
                world.Load();
                world.Display();

                Screen.Camera.Z = 350;

                GlobalSettings.EngineSettings.ShowConsole = true;
            }
            catch (Exception e)
            {
                Logger.CaughtException(e);
            }
        }

        static void LoadFonts()
        {

            if (Assembly.GetEntryAssembly() == null)
                return;

            foreach (string resource in Assembly.GetEntryAssembly().GetManifestResourceNames())
            {
                using (var stream = Assembly.GetEntryAssembly().GetManifestResourceStream(resource))
                {
                    if (stream == null) continue;
                    try
                    {
                        var data = Marshal.AllocCoTaskMem((int)stream.Length);
                        var fontBytes = new byte[stream.Length];

                        stream.Read(fontBytes, 0, (int)stream.Length);
                        Marshal.Copy(fontBytes, 0, data, (int)stream.Length);
                        FontCollection.AddMemoryFont(data, (int)stream.Length);
                        stream.Close();
                        Marshal.FreeCoTaskMem(data);
                    }
                    catch (Exception e)
                    {
                        Logger.CaughtException(e);
                    }
                }
            }
        }
    }
}
