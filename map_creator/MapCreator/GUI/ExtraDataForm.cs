using System;
using System.Collections.Generic;
using System.Windows.Forms;

namespace MapCreator.GUI
{
    public partial class ExtraDataForm : Form
    {
        public KeyValuePair<string, string> Data { get; private set; } 

        public ExtraDataForm(KeyValuePair<string, string> data)
        {
            InitializeComponent();

            Data = data;

            txtKey.Text = data.Key;
            txtValue.Text = data.Value;
        }

        private void btnSave_Click(object sender, EventArgs e)
        {
            Data = new KeyValuePair<string, string>(txtKey.Text, txtValue.Text);
            DialogResult = DialogResult.OK;
            Close();
        }
    }
}
