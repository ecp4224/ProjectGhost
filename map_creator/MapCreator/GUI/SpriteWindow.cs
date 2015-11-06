using System.Drawing;
using System.Windows.Forms;
using MapCreator.App;
using MapCreator.Render;

namespace MapCreator.GUI
{
    public partial class SpriteWindow : Form
    {
        public SpriteGridItem oldItem { get; private set; }
        public Color oldColor;
        public Whatever SelectedWhatever { get; private set; }
        public SpriteWindow()
        {
            InitializeComponent();

            Texture.IdList.ForEach(e =>
            {
                var gridItem = new SpriteGridItem(e);
                gridItem.Click += delegate
                {
                    if (oldItem != null)
                    {
                        oldItem.BackColor = oldColor;
                    }

                    SelectedWhatever = e;
                    oldColor = gridItem.BackColor;
                    gridItem.BackColor = Color.Khaki;
                    oldItem = gridItem;
                };
                flowLayoutPanel1.Controls.Add(gridItem);
            });
        }

        private void btnAdd_Click(object sender, System.EventArgs e)
        {
            if (SelectedWhatever == null)
            {
                MessageBox.Show("Please select a sprite first.", "Hurr durr.");
                return;
            }

            Close();
        }

        private void listBox_SelectedIndexChanged(object sender, System.EventArgs e)
        {

        }

        private void flowLayoutPanel1_Paint(object sender, PaintEventArgs e)
        {

        }
    }
}
