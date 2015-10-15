using System.Windows.Forms;
using MapCreator.App;
using MapCreator.Render;

namespace MapCreator.GUI
{
    public partial class SpriteWindow : Form
    {
        public Whatever SelectedWhatever 
        {
            get
            {
                return (Whatever) listBox.SelectedItem;
            }
        }
        public SpriteWindow()
        {
            InitializeComponent();

            Texture.IdList.ForEach(e => listBox.Items.Add(e));
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
    }
}
