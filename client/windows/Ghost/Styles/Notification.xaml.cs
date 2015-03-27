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
using System.Windows.Markup;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;

namespace Ghost.Styles
{
    [ContentProperty("Description")]
    public partial class Notification : UserControl
    {
        public event EventHandler<RoutedEventArgs> CloseClick;
        public event EventHandler<RoutedEventArgs> AcceptClick; 

        public Notification()
        {
            InitializeComponent();
            AcceptButton.Visibility = Visibility.Hidden;

            CloseButton.Click += delegate
            {
                if (CloseClick != null)
                    CloseClick(this, new RoutedEventArgs());
            };

            AcceptButton.Click += delegate
            {
                if (AcceptClick != null)
                    AcceptClick(this, new RoutedEventArgs());
            };
        }

        public string Title
        {
            get { return (string) GetValue(NotificationTitleProperty); }
            set
            {
                SetValue(NotificationTitleProperty, value);
            }
        }

        public string Description
        {
            get { return (string) GetValue(NotificationDescriptionProperty); }
            set
            {
                SetValue(NotificationDescriptionProperty, value);
            }
        }

        public bool IsRequest
        {
            get { return (bool) GetValue(NotificationIsRequestProperty); }
            set
            {
                SetValue(NotificationIsRequestProperty, value);

                if (!AcceptButton.CheckAccess())
                {
                    AcceptButton.Dispatcher.Invoke(delegate
                    {
                        AcceptButton.Visibility = Visibility.Hidden;
                    });
                }
            }
        }

        public Visibility IsAcceptVisible
        {
            get { return IsRequest ? Visibility.Visible : Visibility.Hidden; }
        }

        public int ID { get; set; }

        public static readonly DependencyProperty NotificationTitleProperty = DependencyProperty.Register("NotificationTitleProperty", typeof(string), typeof(Notification), new PropertyMetadata(default(string), OnTitleChanged));
        public static readonly DependencyProperty NotificationDescriptionProperty = DependencyProperty.Register("NotificationDescriptionProperty", typeof(string), typeof(Notification), new PropertyMetadata(default(string), OnDescriptionChanged));
        public static readonly DependencyProperty NotificationIsRequestProperty = DependencyProperty.Register("NotificationIsRequestProperty", typeof(bool), typeof(Notification), new PropertyMetadata(default(bool), OnIsRequestChanged));

        private static void OnIsRequestChanged(DependencyObject d, DependencyPropertyChangedEventArgs e)
        {
            var n = (Notification)d;

            n.AcceptButton.Visibility = n.IsAcceptVisible;
        }

        private static void OnDescriptionChanged(DependencyObject d, DependencyPropertyChangedEventArgs e)
        {
            var n = (Notification) d;

            n.DescriptionLabel.Text = n.Description;
        }

        private static void OnTitleChanged(DependencyObject d, DependencyPropertyChangedEventArgs e)
        {
            var n = (Notification)d;

            n.TitleLabel.Content = n.Title;
        }
    }
}
