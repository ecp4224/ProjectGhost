using System;
using System.Diagnostics;
using System.Threading;
using System.Windows;

namespace Ghost.Core
{
    public static class Extensions
    {
        public static void LaunchGameClient(this Window window, Action onClientClosed = null)
        {
            if (!window.Dispatcher.CheckAccess())
            {
                window.Dispatcher.Invoke(() => LaunchGameClient(window, onClientClosed));
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
                    "\"" + GhostApi.Domain + "\" \"" + GhostApi.Session + "\" 2",
                WindowStyle = ProcessWindowStyle.Hidden,
                CreateNoWindow = true,
                FileName = "game.exe"
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
