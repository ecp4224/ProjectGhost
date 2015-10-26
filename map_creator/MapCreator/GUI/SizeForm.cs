using System;
using System.Drawing;
using System.Windows.Forms;
using MapCreator.Render;

namespace MapCreator.GUI
{
    public partial class SizeForm : Form
    {
        private MainWindow form;
        public SizeForm(MainWindow form)
        {
            InitializeComponent();
            this.form = form;
        }

        private void SizeForm_Load(object sender, EventArgs e)
        {
            widthNum.Value = (decimal) Game.Width + 216;
            heightNum.Value = (decimal) Game.Height + 61;
        }

        private void accept_Click(object sender, EventArgs e)
        {
            form.SetSize(new Size((int) (widthNum.Value + 216), (int) (heightNum.Value + 61)));
            Close();
        }
    }
}
