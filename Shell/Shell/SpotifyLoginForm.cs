using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace Shell
{
    public partial class SpotifyLoginForm : Form
    {
        public SpotifyLoginForm()
        {
            InitializeComponent();
        }
        public string UserName
        {
            get
            {
                return this.textBox1.Text;
            }
        }
        public string Password
        {
            get
            {
                return this.textBox2.Text;
            }
        }
        private void button1_Click(object sender, EventArgs e)
        {
            button1.Enabled = false;
            bool loggedIn = SoundBounce.SpotifyAPI.Spotify.Login(UserName, Password);
            if (loggedIn)
            {
                this.DialogResult = DialogResult.OK;
                this.Close();
            } else
            {
                button1.Enabled = true;
            }
        }
    }
}
