using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
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
using MahApps.Metro.Controls.Dialogs;

namespace Ghost
{
    /// <summary>
    /// Interaction logic for MainWindow.xaml
    /// </summary>
    public partial class MainWindow
    {
        public MainWindow()
        {
            InitializeComponent();
        }

        private void ButtonBase_OnClick(object sender, RoutedEventArgs e)
        {
            this.LaunchGameClient();
        }

        private void MainWindow_OnLoaded(object sender, RoutedEventArgs e)
        {
            QueueTypesView.Items.Add("Casual");
            QueueTypesView.Items.Add("Ranked");
            QueueTypesView.Items.Add("Random");

            QueueNamesView.Items.Add("1 v 1");
            QueueNamesView.Items.Add("2 v 2");
            QueueNamesView.Items.Add("3 v 3");
        }
    }
}
