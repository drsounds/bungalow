using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;
using CefSharp.WinForms;
using SoundBounce.WindowsClient;

namespace Shell
{
    public partial class Form1 : Form
    {
        public Form1()
        {
            InitializeComponent();
            this.menuStrip1.Renderer = new BungalowToolStripRenderer();
        }

        private void Renderer_RenderMenuItemBackground(object sender, ToolStripItemRenderEventArgs e)
        {
            e.Graphics.FillRectangle(new SolidBrush(Color.FromArgb(255, 44, 44, 44)), e.ToolStrip.Bounds);
        }

        private void Renderer_RenderToolStripBackground(object sender, ToolStripRenderEventArgs e)
        {
            e.Graphics.FillRectangle(new SolidBrush(Color.FromArgb(255, 44, 44, 44)), e.ToolStrip.Bounds);
        }

        private void Form1_Load(object sender, EventArgs e)
        {
            var browser = new ChromiumWebBrowser("http://play.bungalow.qi")
            {
                Dock = DockStyle.Fill
            };
            browser.BrowserSettings.BackgroundColor = 0xff000000;
            panel5.Controls.Add(browser);
            browser.RegisterJsObject("spotifyPlayer", new SpotifyBrowserAPI(), true);

            if (!SoundBounce.SpotifyAPI.Spotify.IsLoggedIn)
            {
                if (new SpotifyLoginForm().ShowDialog() != DialogResult.OK)
                {
                    Close();
                }
            }
        }

        private void panel5_Paint(object sender, PaintEventArgs e)
        {

        }

        private void panel1_Paint(object sender, PaintEventArgs e)
        {
        }

        private void label1_Click(object sender, EventArgs e)
        {

        }
    }
}
