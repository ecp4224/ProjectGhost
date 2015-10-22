using System;
using System.Drawing;
using System.IO;
using System.Windows.Forms;
using MapCreator.App;
using MapCreator.Render;
using MapCreator.Render.Sprite;
using MouseEventArgs = System.Windows.Forms.MouseEventArgs;

namespace MapCreator.GUI
{
    public partial class MainWindow : Form
    {
        private bool _loaded;

        private readonly Game _game = new Game();

        private readonly System.Timers.Timer _timer = new System.Timers.Timer(50.0f);

        private const double Rad = Math.PI / 180.0;

        public MainWindow()
        {
            InitializeComponent();

            Texture.Load();

            _timer.Elapsed += (sender, args) =>
            {
                glControl.Invalidate();
            };
            _timer.Start();
        }

        private void glControl_Load(object sender, EventArgs e)
        {
            _loaded = true;

            _game.Initialize(glControl.Width, glControl.Height);
            _game.SetControls(spriteList);
        }

        private void glControl_Paint(object sender, PaintEventArgs e)
        {
            if (!_loaded) { return; }

            _game.Render();

            glControl.SwapBuffers();
        }

        private void glControl_Resize(object sender, EventArgs e)
        {
            _game.Resize(glControl.Width, glControl.Height);
        }

        private void propertyGrid_PropertyValueChanged(object s, PropertyValueChangedEventArgs e)
        {
            spriteList.Items[spriteList.Items.IndexOf(propertyGrid.SelectedObject)] = propertyGrid.SelectedObject;
        }

        private void spriteList_SelectedIndexChanged(object sender, EventArgs e)
        {
            if (spriteList.SelectedItem == null)
            {
                return;
            }

            if (propertyGrid.SelectedObject != null)
            {
                ((MapObject) propertyGrid.SelectedObject).Color = Color.White;
            }

            ((MapObject)spriteList.SelectedItem).Color = Color.LightSalmon;

            propertyGrid.SelectedObject = spriteList.SelectedItem;
        }

        private void btnAdd_Click(object sender, EventArgs e)
        {
            var window = new SpriteWindow();
            window.ShowDialog();

            if (window.SelectedWhatever == null) { return; }

            var sprite = new MapObject(window.SelectedWhatever.Id);
            _game.AddSprite(sprite);

            spriteList.Items.Add(sprite);
            propertyGrid.SelectedObject = sprite;
        }

        private void glControl_MouseDoubleClick(object sender, MouseEventArgs e)
        {
            _game.HandleClick(e.X, -e.Y + (int) Game.Height);
        }

        private bool _mouseDown;
        private int _ox;
        private int _oy;

        private void glControl_MouseUp(object sender, MouseEventArgs e)
        {
            _mouseDown = false;

            if (spriteList.SelectedIndex != -1)
            {
                propertyGrid.SelectedObject = spriteList.SelectedItem;
            }
        }

        private void glControl_MouseDown(object sender, MouseEventArgs e)
        {
            _mouseDown = true;
            _ox = e.X;
            _oy = -e.Y + (int) Game.Height;
        }

        private void glControl_MouseMove(object sender, MouseEventArgs e)
        {
            if (!_mouseDown || spriteList.SelectedIndex == -1) { return; }

            var sprite = (MapObject) spriteList.SelectedItem;

            if (!sprite.Contains(e.X, -e.Y + (int) Game.Height)) { return; }

            var dx = e.X - _ox;
            var dy = -e.Y + (int) Game.Height - _oy;
           
            sprite.X += dx;
            sprite.Y += dy;        

            _ox = e.X;
            _oy = -e.Y + (int) Game.Height;
        }

        private void glControl_MouseWheel(object sender, MouseEventArgs e)
        {
            if (spriteList.SelectedIndex == -1) { return; }

            
            ((MapObject) spriteList.SelectedItem).Rotation += 2 * Math.Sign(e.Delta);
            propertyGrid.SelectedObject = spriteList.SelectedItem;
        }

        private void glControl_PreviewKeyDown(object sender, PreviewKeyDownEventArgs e)
        {
            if (spriteList.SelectedIndex == -1) { return; }

            var sprite = (MapObject) spriteList.SelectedItem;

            switch (e.KeyCode) { 
                case Keys.Up:
                    sprite.Y--;
                    break;
                case Keys.Down:
                    sprite.Y++;
                    break;
                case Keys.Left:
                    sprite.X--;
                    break;
                case Keys.Right:
                    sprite.X++;
                    break;
            }
        }

        private void glControl_KeyUp(object sender, KeyEventArgs e)
        {
            propertyGrid.SelectedObject = spriteList.SelectedItem;
        }

        private void saveToolStripMenuItem_Click(object sender, EventArgs e)
        {
            if (string.IsNullOrEmpty(saveFileDialog.FileName) && saveFileDialog.ShowDialog() != DialogResult.OK)
            {
                return;
            }

            var path = saveFileDialog.FileName;
            File.WriteAllText(path, _game.Map.Json);
        }

        private void saveAsToolStripMenuItem_Click(object sender, EventArgs e)
        {
            if (saveFileDialog.ShowDialog() != DialogResult.OK) { return; }

            var path = saveFileDialog.FileName;
            File.WriteAllText(path, _game.Map.Json);
        }

        private void openMapToolStripMenuItem_Click(object sender, EventArgs e)
        {
            if (openFileDialog.ShowDialog() != DialogResult.OK) { return; }

            var path = openFileDialog.FileName;
            _game.Open(path);
        }
    }
}
