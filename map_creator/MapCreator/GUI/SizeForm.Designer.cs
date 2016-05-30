namespace MapCreator.GUI
{
    partial class SizeForm
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.widthNum = new System.Windows.Forms.NumericUpDown();
            this.heightNum = new System.Windows.Forms.NumericUpDown();
            this.accept = new System.Windows.Forms.Button();
            this.label1 = new System.Windows.Forms.Label();
            this.label2 = new System.Windows.Forms.Label();
            ((System.ComponentModel.ISupportInitialize)(this.widthNum)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.heightNum)).BeginInit();
            this.SuspendLayout();
            // 
            // widthNum
            // 
            this.widthNum.Location = new System.Drawing.Point(32, 21);
            this.widthNum.Maximum = new decimal(new int[] {
            1920,
            0,
            0,
            0});
            this.widthNum.Name = "widthNum";
            this.widthNum.Size = new System.Drawing.Size(120, 20);
            this.widthNum.TabIndex = 0;
            // 
            // heightNum
            // 
            this.heightNum.Location = new System.Drawing.Point(32, 65);
            this.heightNum.Maximum = new decimal(new int[] {
            1080,
            0,
            0,
            0});
            this.heightNum.Name = "heightNum";
            this.heightNum.Size = new System.Drawing.Size(120, 20);
            this.heightNum.TabIndex = 1;
            // 
            // accept
            // 
            this.accept.Location = new System.Drawing.Point(49, 102);
            this.accept.Name = "accept";
            this.accept.Size = new System.Drawing.Size(75, 23);
            this.accept.TabIndex = 2;
            this.accept.Text = "Update";
            this.accept.UseVisualStyleBackColor = true;
            this.accept.Click += new System.EventHandler(this.accept_Click);
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(29, 49);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(41, 13);
            this.label1.TabIndex = 3;
            this.label1.Text = "Height:";
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(29, 5);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(38, 13);
            this.label2.TabIndex = 4;
            this.label2.Text = "Width:";
            // 
            // SizeForm
            // 
            this.AcceptButton = this.accept;
            this.AccessibleRole = System.Windows.Forms.AccessibleRole.MenuPopup;
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(181, 137);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.accept);
            this.Controls.Add(this.heightNum);
            this.Controls.Add(this.widthNum);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedToolWindow;
            this.MaximizeBox = false;
            this.MinimizeBox = false;
            this.Name = "SizeForm";
            this.ShowIcon = false;
            this.Text = "SizeForm";
            this.TopMost = true;
            this.Load += new System.EventHandler(this.SizeForm_Load);
            ((System.ComponentModel.ISupportInitialize)(this.widthNum)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.heightNum)).EndInit();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.NumericUpDown widthNum;
        private System.Windows.Forms.NumericUpDown heightNum;
        private System.Windows.Forms.Button accept;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Label label2;
    }
}