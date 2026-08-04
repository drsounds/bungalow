package se.spacify.controls;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.JTree;
import javax.swing.tree.TreeNode;

import se.spacify.ui.theme.ThemeManager;

/**
 * A tree control. Independent of any UI toolkit at the type level: the active
 * {@link se.spacify.ui.render.UserInterface} creates this control's native
 * peer — currently only implemented for Swing (a {@link JTree}, painted by
 * the active skin via {@link se.spacify.skinning.Skin#paintTree}, see
 * {@link #createSwingPeer()}). Bridging {@link TreeNode}-based data to a
 * fundamentally different Jexer tree widget is a larger follow-up; the Jexer
 * backend simply doesn't support {@code Tree} yet. Swing-only code that needs
 * the concrete widget can use {@link #getSwingComponent()}.
 */
public class Tree extends Control<Object> {

	private TreeNode root;

	public Tree() {
		initNative();
	}

	public Tree(TreeNode root) {
		this.root = root;
		initNative();
	}

	// ── Swing peer (used only by se.spacify.ui.render.swing.SwingUserInterface) ───

	protected class Surface extends JTree {
		private static final long serialVersionUID = 1L;
		Surface() { super(); }
		Surface(TreeNode root) { super(root); }
		@Override
		protected void paintComponent(Graphics g) {
			Tree.this.paintSurface(g, this);
		}
		void superPaint(Graphics g) { super.paintComponent(g); }
	}

	protected void paintSurface(Graphics g, JTree tree) {
		Graphics2D g2 = (Graphics2D) g.create();
		getSkin().paintTree(this, g2);
		g2.dispose();
		// Selection highlight spanning the full row width (a plain JTree only fills
		// behind the label). The cell renderer paints transparently on top.
		int[] selected = tree.getSelectionRows();
		if (selected != null) {
			g.setColor(ThemeManager.getAccentForegroundColor());
			for (int row : selected) {
				Rectangle b = tree.getRowBounds(row);
				if (b != null) g.fillRect(0, b.y, tree.getWidth(), b.height);
			}
		}
		if (getComponent() instanceof Surface s) s.superPaint(g);
	}

	/** Builds this tree's Swing peer. Called only by {@code SwingUserInterface}. */
	public JTree createSwingPeer() {
		return root != null ? new Surface(root) : new Surface();
	}

	public JTree getSwingComponent() {
		return getComponent() instanceof JTree t ? t : null;
	}
}
