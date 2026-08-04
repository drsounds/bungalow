package se.spacify.controls;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JSplitPane;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

/**
 * A split-pane container control. Independent of any UI toolkit: the active
 * {@link se.spacify.ui.render.UserInterface} creates this control's native
 * peer (a {@link JSplitPane} for Swing, see {@link #createSwingPeer()}, with a
 * skinned divider painted via {@link se.spacify.skinning.Skin#paintSplitPaneDivider};
 * a Jexer {@code TSplitPane} for the Jexer backend). Swing-only code that
 * needs the concrete widget can use {@link #getSwingComponent()}.
 */
public class SplitPane extends Control<Object> {

	public static final int HORIZONTAL_SPLIT = JSplitPane.HORIZONTAL_SPLIT;
	public static final int VERTICAL_SPLIT   = JSplitPane.VERTICAL_SPLIT;

	private int orientation = HORIZONTAL_SPLIT;
	/** Only the {@link #SplitPane(int, Object, Object)} constructor installs the skinned divider. */
	private boolean skinnedDivider = false;

	public SplitPane() {
		initNative();
	}

	/**
	 * @param orientation {@link #HORIZONTAL_SPLIT} (left/right) or {@link #VERTICAL_SPLIT} (top/bottom)
	 * @param left  a {@link Control} or a raw {@link Component} (Swing only)
	 * @param right a {@link Control} or a raw {@link Component} (Swing only)
	 */
	public SplitPane(int orientation, Object left, Object right) {
		this.orientation = orientation;
		this.skinnedDivider = true;
		initNative();
		mountSide(left);
		mountSide(right);
	}

	public int getOrientation() {
		return orientation;
	}

	/** {@code left} then {@code right} become this split's first and second child, respectively. */
	private void mountSide(Object side) {
		if (side instanceof Control<?> c) {
			add(c);
		} else if (side instanceof Component comp && getComponent() instanceof JSplitPane sp) {
			// Raw AWT component escape hatch (Swing only); never exercised by the
			// app today (every call site passes a Control), kept for API parity
			// with the original class.
			if (sp.getLeftComponent() == null) sp.setLeftComponent(comp);
			else                               sp.setRightComponent(comp);
		}
	}

	// ── Swing peer (used only by se.spacify.ui.render.swing.SwingUserInterface) ───

	/** The skinned divider; painted by the active skin. */
	public class SplitPaneDivider extends BasicSplitPaneDivider {
		private static final long serialVersionUID = 1L;
		public SplitPaneDivider(BasicSplitPaneUI ui) {
			super(ui);
			super.setBorder(null);
		}
		@Override public void setBorder(Border border) { /* ignore */ }
		@Override
		public void paint(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			getTaste().getSkin().paintSplitPaneDivider(this, g2);
			g2.dispose();
		}
	}

	private class SplitPaneDividerUI extends BasicSplitPaneUI {
		@Override
		public BasicSplitPaneDivider createDefaultDivider() {
			return new SplitPaneDivider(this);
		}
	}

	/** Builds this split pane's Swing peer. Called only by {@code SwingUserInterface}. */
	public JSplitPane createSwingPeer() {
		JSplitPane sp = new JSplitPane(orientation);
		if (skinnedDivider) {
			sp.setUI(new SplitPaneDividerUI());
		} else {
			sp.setOpaque(false);
		}
		BasicSplitPaneDivider d = dividerOf(sp);
		if (d != null) d.setBorder(new EmptyBorder(0, 0, 0, 0));
		return sp;
	}

	public JSplitPane getSwingComponent() {
		return getComponent() instanceof JSplitPane sp ? sp : null;
	}

	public BasicSplitPaneDivider getDivider() {
		return getSwingComponent() != null ? dividerOf(getSwingComponent()) : null;
	}

	private static BasicSplitPaneDivider dividerOf(JSplitPane sp) {
		return sp.getUI() instanceof BasicSplitPaneUI ui ? ui.getDivider() : null;
	}
}
