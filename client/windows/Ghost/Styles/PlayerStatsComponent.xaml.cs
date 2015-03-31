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
using System.Windows.Navigation;
using System.Windows.Shapes;

namespace Ghost.Styles
{
    /// <summary>
    /// Interaction logic for PlayerStatsComponent.xaml
    /// </summary>
    public partial class PlayerStatsComponent : UserControl
    {
        private string _accuracy;
        public string Accuracy
        {
            get { return _accuracy;  }
            set {
                this.lblAccuracy.Content = value;
                _accuracy = value;
            }
        }

        public PlayerStatsComponent()
        {
            InitializeComponent();
            DataContext = this;
        }
    }
}
