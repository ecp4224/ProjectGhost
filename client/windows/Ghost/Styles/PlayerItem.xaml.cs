using System;
using System.Collections.Generic;
using System.Globalization;
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
    /// Interaction logic for PlayerItem.xaml
    /// </summary>
    public partial class PlayerItem : UserControl
    {
        public PlayerItem() : this("", 0, 0, 0, 0.0, 0, 0)
        {
        }

        public PlayerItem(string displayName, int gamesWon, int gamesLost, int playersKilled, double accuracy, int hatTricks, int friendCount)
        {
            this.GamesLost = gamesLost.ToString(CultureInfo.InvariantCulture);
            this.GamesWon = gamesWon.ToString(CultureInfo.InvariantCulture);
            this.PlayersKilled = playersKilled.ToString(CultureInfo.InvariantCulture);
            this.Accuracy = Math.Round(accuracy * 100.0, 2) + "%";
            this.HatTricks = hatTricks.ToString(CultureInfo.InvariantCulture);
            this.FriendCount = friendCount.ToString(CultureInfo.InvariantCulture);
            this.DisplayName = displayName;

            InitializeComponent();

            this.DataContext = this;
        }

        public string DisplayName { get; private set; }

        public string GamesWon { get; private set; }

        public string GamesLost { get; private set; }

        public string Accuracy { get; private set; }

        public string FriendCount { get; private set; }

        public string PlayersKilled { get; private set; }

        public string HatTricks { get; private set; }
    }
}
