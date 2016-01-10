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
namespace Shell
{
    public partial class Form1 : Form
    {
        public Form1()
        {
            InitializeComponent();
        }

        private void Form1_Load(object sender, EventArgs e)
        {
            var browser = new ChromiumWebBrowser("http://play.bungalow.qi")
            {
                Dock = DockStyle.Fill
            };
            browser.BrowserSettings.BackgroundColor = 0xff000000;
            panel5.Controls.Add(browser);
        }

        private void panel5_Paint(object sender, PaintEventArgs e)
        {

        }
    }
}
