using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Drawing;
using System.Data;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;
using MapCreator.App;

namespace MapCreator.GUI
{
    public partial class SpriteGridItem : UserControl
    {
        public SpriteGridItem(Whatever wat = null)
        {
            InitializeComponent();

            if (wat != null)
            {
                label1.Text = wat.Name;
                pictureBox1.Image = Image.FromFile(wat.Path);
            }
        }

        private void pictureBox1_Click(object sender, EventArgs e)
        {
            OnClick(e);
        }

        private void label1_Click(object sender, EventArgs e)
        {
            OnClick(e);
        }
    }
}
