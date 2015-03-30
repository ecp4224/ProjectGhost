using System;
using System.Diagnostics;
using System.Threading;
using System.Windows;

namespace Ghost.Core
{
    public static class Extensions
    {
        public static void LaunchGameClient(this Window window, QueueType queueToJoin, GameOptions options = null, Action onClientClosed = null)
        {
            LaunchGameClient(window, (byte)queueToJoin, options, onClientClosed);
        }

        public static void LaunchGameClient(this Window window, byte queueToJoin, GameOptions options = null, Action onClientClosed = null)
        {
            if (options == null)
                options = new GameOptions();

            if (!window.Dispatcher.CheckAccess())
            {
                window.Dispatcher.Invoke(() => LaunchGameClient(window, queueToJoin, options, onClientClosed));
                return;
            }

            if (GhostApi.TcpClient != null && GhostApi.TcpClient.Connected)
            {
                GhostApi.TcpClient.Close();
                GhostApi.TcpStream.Close();
                Thread.Sleep(1500);
            }

            var info = new ProcessStartInfo
            {
                Arguments =
                    "\"" + GhostApi.Domain + "\" \"" + GhostApi.Session + "\" " + queueToJoin + " " + options,
                FileName = "game.exe",
                WindowStyle = ProcessWindowStyle.Normal
            };
            var process = Process.Start(info);
            if (process == null)
                return;

            window.Hide();

            new Thread(new ThreadStart(async delegate
            {
                process.WaitForExit();

                await GhostApi.ConnectTCP();

                if (onClientClosed != null) onClientClosed();

                window.Dispatcher.Invoke(window.Show);
            })).Start();
        }
    }
}
