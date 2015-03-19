using System;
using System.Diagnostics;
using System.Threading;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Input;
using Ghost.Core;
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
            Task<Result<bool>> task = GhostApi.Login(UsernameBox.Text, PasswordBox.Password);

            var progress = await this.ShowProgressAsync("Signing In", "Please wait while your login info is verified...");
            progress.SetIndeterminate();

            Result<bool> result = await task;

            if (!result.Value)
            {
                await progress.CloseAsync();
                await this.ShowMessageAsync("Could not login", result.Reason);
            }
            else
            {
                progress.SetTitle("Connecting..");
                progress.SetMessage("Please wait while a connection to the server is made..");
                Connect();
            }
        }

        private async void Connect()
        {
            var result = await GhostApi.ConnectTCP();

            if (!result.Value)
            {
                await this.ShowMessageAsync("Failed to connect", "Could not connect to the server. Please check your internet and try again!");
                return;
            }

            new MainWindow().Show();
            Close();
        }

        private void SignUpButton_OnClick(object sender, RoutedEventArgs e)
        {
            Register();
        }

        private async void Register()
        {

            var dialog = await this.ShowLoginAsync("Register", "Please choose a username and a password.", new LoginDialogSettings
            {
                AffirmativeButtonText = "Register"
            });
            
            if (string.IsNullOrWhiteSpace(dialog.Username) || string.IsNullOrWhiteSpace(dialog.Password))
                return;

            var progress =
                await this.ShowProgressAsync("Creating account", "Please wait while your account is created..");
            progress.SetIndeterminate();

            var results = await GhostApi.Register(dialog.Username, dialog.Password);

            if (!results.Value)
            {
                await progress.CloseAsync();

                await this.ShowMessageAsync("Error",
                            results.Reason);
                return;
            }
            
            results = await GhostApi.Login(dialog.Username, dialog.Password);

            if (!results.Value)
            {
                await progress.CloseAsync();

                await this.ShowMessageAsync("Could not login", results.Reason);
                return;
            }

            results = await GhostApi.ConnectTCP();

            if (!results.Value)
            {
                await progress.CloseAsync();

                await this.ShowMessageAsync("Error", results.Reason);
                return;
            }

            await progress.CloseAsync();
            while (true)
            {
                var name = await
                    this.ShowInputAsync("Display Name",
                        "Choose a display name. This is the name that will appear in-game.", new MetroDialogSettings
                        {
                            AffirmativeButtonText = "Claim!"
                        });

                if (string.IsNullOrWhiteSpace(name))
                    break;

                progress =
                    await this.ShowProgressAsync("Claiming", "Please wait while your display name is set...");
                progress.SetIndeterminate();

                results = await GhostApi.ChangeDisplayName(name);

                if (!results.Value)
                {
                    await progress.CloseAsync();

                    await
                        this.ShowMessageAsync("Taken!",
                            "The display name you choose is already taken! Please try another display name.");
                    continue;
                }

                new MainWindow().Show();
                Close();
                break;
            }
        }

        private void LoginWindow_OnLoaded(object sender, RoutedEventArgs e)
        {
            PasswordBox.Password = "AHINTTEXT";

            new Thread(new ThreadStart(delegate
            {
                Thread.Sleep(2000);

                if (ECPLauncher.Updater.IsUpdateReady())
                {
                    Dispatcher.Invoke(async delegate
                    {
                        await
                            this.ShowProgressAsync("Updater",
                                "A patch was found...please wait while the patch is applied..");

                        new Thread(new ThreadStart(delegate
                        {
                            Thread.Sleep(3000);
                            var process = new Process();
                            var process_info = new ProcessStartInfo
                            {
                                FileName = "updater.exe",
                                WindowStyle = ProcessWindowStyle.Normal
                            };
                            process.StartInfo = process_info;

                            process.Start();
                            Environment.Exit(3);
                        })).Start();
                    });
                }
            })).Start();
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

        private void PasswordBox_OnKeyDown(object sender, KeyEventArgs e)
        {
            if (e.Key == Key.Enter && e.IsDown)
            {
                Login();
            }
        }

        private void UsernameBox_OnKeyDown(object sender, KeyEventArgs e)
        {
            if ((e.Key == Key.Enter || e.Key == Key.Tab) && e.IsDown)
            {
                PasswordBox.Focus();
            }
        }
    }
}
