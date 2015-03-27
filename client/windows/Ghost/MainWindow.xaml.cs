﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Shapes;
using Ghost.Core;
using Ghost.Styles;
using MahApps.Metro.Controls.Dialogs;

namespace Ghost
{
    /// <summary>
    /// Interaction logic for MainWindow.xaml
    /// </summary>
    public partial class MainWindow
    {
        private IEnumerable<QueueInfo>[] sources;
        private bool loaded;
        private Dictionary<int, Request> requests = new Dictionary<int, Request>(); 
        public MainWindow()
        {
            InitializeComponent();
        }

        private void ButtonBase_OnClick(object sender, RoutedEventArgs e)
        {
            var info = QueueNamesView.Items[QueueNamesView.SelectedIndex] as QueueInfo;
            if (info == null || info.Type == QueueType.Unknown)
                return;

            this.LaunchGameClient(info.Type);
        }

        private void MainWindow_OnLoaded(object sender, RoutedEventArgs e)
        {
            QueueTypesView.Items.Add("Loading...");
            LoadQueues();

            new Thread(new ThreadStart(delegate
            {
                var callbacks = new Callbacks(Dispatcher)
                {
                    OnNewRequest = OnNewRequest,
                    OnRequestRemoved = OnRequestRemoved
                };

                GhostApi.ReadPackets(callbacks);
            })).Start();
        }

        private void OnRequestRemoved(int i)
        {
            if (requests.ContainsKey(i))
                requests.Remove(i);
        }

        private void OnNewRequest(Request request)
        {
            requests.Add(request.RequestId, request);
        }

        private void UpdateNotification()
        {
            NotificationGrid.Children.Clear();

            foreach (var n in requests.Keys.Select(id => requests[id]).Select(request => new Notification()
            {
                Title = request.Title,
                Description = request.Description,
                IsRequest = true
            }))
            {
                NotificationGrid.Children.Add(n);
            }
        }

        private async void LoadQueues()
        {
            var textInfo = Thread.CurrentThread.CurrentCulture.TextInfo;
            var results = await GhostApi.GetQueues();

            QueueTypesView.Items.Clear();

            if (results.Value.Count > 0)
            {
                sources = new IEnumerable<QueueInfo>[results.Value.Count];

                int i = 0;
                foreach (string types in results.Value.Keys)
                {
                    if (results.Value[types].Length == 0 || results.Value[types].All(r => r == null) || results.Value[types][0].Type == QueueType.Unknown || results.Value[types][0].Type == QueueType.Private)
                        continue;

                    QueueTypesView.Items.Add(textInfo.ToTitleCase(types));
                    sources[i] = results.Value[types];
                    i++;
                }

                loaded = true;
            }
            else
            {
                QueueTypesView.Items.Add("Error: " + results.Reason);
            }
        }

        private void QueueTypesView_OnSelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            if (loaded)
            {
                QueueNamesView.ItemsSource = QueueTypesView.SelectedIndex > -1 ? sources[QueueTypesView.SelectedIndex] : null;
            }
        }

        private void NotificationButton_OnClick(object sender, RoutedEventArgs e)
        {
            Friends.IsOpen = false;
            Notifications.IsOpen = !Notifications.IsOpen;
        }

        private void FriendButton_OnClick(object sender, RoutedEventArgs e)
        {
            Notifications.IsOpen = false;
            Friends.IsOpen = !Friends.IsOpen;
        }
    }
}
