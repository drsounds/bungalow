using System;
using System.Collections.Generic;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace Shell
{
    class BungalowToolStripRenderer : System.Windows.Forms.ToolStripRenderer 
    {
        protected override void OnRenderToolStripBackground(ToolStripRenderEventArgs e)
        {
            e.Graphics.FillRectangle(new SolidBrush(ColorTranslator.FromHtml("#666")), e.ToolStrip.Bounds);
        }
        protected override void OnRenderMenuItemBackground(ToolStripItemRenderEventArgs e)
        {
            if (e.Item.Selected)
            {
                e.Graphics.FillRectangle(new SolidBrush(ColorTranslator.FromHtml("#777")), e.Item.Bounds);
            }
        }
        protected override void OnRenderItemBackground(ToolStripItemRenderEventArgs e)
        {
           if (e.Item.Selected)
            {
                e.Graphics.FillRectangle(new SolidBrush(ColorTranslator.FromHtml("#777")), e.Item.Bounds);
            }
        }
        protected override void OnRenderItemText(ToolStripItemTextRenderEventArgs e)
        {
            if (e.Item.GetCurrentParent() == null)
            {
                base.OnRenderItemText(e);
                return;
            }
            e.Graphics.DrawString(e.Text, new Font("MS Sans Serif", 8), new SolidBrush(Color.White), new PointF( (float)e.TextRectangle.Left, (float)e.TextRectangle.Top));
        }
    }
}
