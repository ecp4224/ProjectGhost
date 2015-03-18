using System;
using System.Diagnostics;
using System.Threading;
using System.Threading.Tasks;
using System.Windows;
using MahApps.Metro.Controls.Dialogs;

namespace Ghost
{
    public partial class LoginWindow
    {
        public LoginWindow()
        {
            InitializeComponent();
        }

        private void ButtonBase_OnClick(object sender, RoutedEventArgs e)
        {
            Login();
        }

        private async void Login()
        {
            Task<Result<bool>> task = GhostWebAPI.Login(UsernameBox.Text, PasswordBox.Password);

            var progress = await this.ShowProgressAsync("Signing In", "Please wait while your login info is verified...");
            progress.SetIndeterminate();

            Result<bool> result = await task;

            if (!result.Value)
            {
                await progress.CloseAsync();
                await this.ShowMessageAsync("Bad login", "The username/password entered is incorrect. Please try again");
            }
            else
            {
                await progress.CloseAsync();
                Finish();
                //TODO Maybe don't connect via TCP ?
                //progress.SetTitle("Connecting..");
                //progress.SetMessage("Please wait while a connection to the server is made..");
                //Connect();
            }
        }

        private async void Connect()
        {
            var result = await GhostWebAPI.ConnectTCP();

            if (!result.Value)
            {
                await this.ShowMessageAsync("Bad login", "The username/password entered is incorrect. Please try again");
                return;
            }

            Finish();
        }

        private void SignUpButton_OnClick(object sender, RoutedEventArgs e)
        {
            Register();
        }

        private async void Register()
        {
            var dialog = await this.ShowLoginAsync("Register", "Please choose a username and a password.");
            
            if (string.IsNullOrWhiteSpace(dialog.Username) || string.IsNullOrWhiteSpace(dialog.Password))
                return;

            var progress =
                await this.ShowProgressAsync("Creating account", "Please wait while your account is created..");
            progress.SetIndeterminate();

            var results = await GhostWebAPI.Register(dialog.Username, dialog.Password);

            if (results.Value) //Register result
            {
                progress.SetTitle("Signing In");
                progress.SetMessage("Please wait while your login info is verified...");

                results = await GhostWebAPI.Login(dialog.Username, dialog.Password);

                if (results.Value) //Login result
                {
                    progress.SetTitle("Connecting..");
                    progress.SetMessage("Please wait while a connection to the server is made..");

                    results = await GhostWebAPI.ConnectTCP();

                    await progress.CloseAsync();
                    if (results.Value) //Connecting result
                    {
                        do
                        {
                            var displayName = await
                                this.ShowInputAsync("Display Name",
                                    "Please choose a display name. This is what you will appear in-game as.");
                            if (!string.IsNullOrWhiteSpace(displayName))
                            {
                                progress = await this.ShowProgressAsync("Finalizing",
                                    "Please wait while your account is finalized..");
                                progress.SetIndeterminate();

                                results = await GhostWebAPI.ChangeDisplayName(displayName);

                                await progress.CloseAsync();
                                if (results.Value) //Display result
                                {
                                    Finish();
                                    return; 
                                }

                                await this.ShowMessageAsync("Error", "That display name is already taken!");
                            }
                        } while (true);
                    }
                    await
                        this.ShowMessageAsync("Error",
                            "There was an error connecting the server. Check your internet connection and try again.");
                    return;
                }
                await
                    this.ShowMessageAsync("Error",
                            "There was an signing in. Check your internet connection and try again.");
                return;
            }
            await
                this.ShowMessageAsync("Error",
                            results.Reason);
            return;
        }

        private void LoginWindow_OnLoaded(object sender, RoutedEventArgs e)
        {
            PasswordBox.Password = "AHINTTEXT";
        }

        private void PasswordBox_OnGotFocus(object sender, RoutedEventArgs e)
        {
            if (PasswordBox.Password == "AHINTTEXT")
                PasswordBox.Password = "";
        }

        private void PasswordBox_OnLostFocus(object sender, RoutedEventArgs e)
        {
            if (string.IsNullOrWhiteSpace(PasswordBox.Password))
                PasswordBox.Password = "AHINTTEXT";
        }

        private void Finish()
        {
            if (GhostWebAPI.TcpClient != null && GhostWebAPI.TcpClient.Connected)
            {
                GhostWebAPI.TcpClient.Close();
                GhostWebAPI.TcpStream.Close();
            }


            var info = new ProcessStartInfo
            {
                Arguments =
                    "\"" + GhostWebAPI.Domain + "\" \"" + GhostWebAPI.Session + "\" 2",
                WindowStyle = ProcessWindowStyle.Hidden,
                CreateNoWindow = true,
                FileName = "game.exe"
            };
            var process = Process.Start(info);
            if (process == null)
                return;

            Hide();

            new Thread(new ThreadStart(delegate
            {
                process.WaitForExit();

                Dispatcher.Invoke(Show);
            })).Start();
        }
    }
}
