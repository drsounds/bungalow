package se.spacify.ui;

import se.spacify.controls.GlossyButton;

import se.spacify.navigation.NavigationListener;
import se.spacify.navigation.ViewStack;

import se.spacify.controls.Panel;
import se.spacify.controls.Button;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class AppHeader extends Panel implements NavigationListener {

	protected static final Color CHROME_DARK = new Color(14, 14, 14);
	protected static final Color HIGHLIGHT = new Color(255, 255, 255, 35);

	protected ViewStack viewStack;
	protected GlossyButton backBtn;
	protected GlossyButton forwardBtn;
	protected Button sidebarToggle;
	/** Cached store favicons, fetched off the EDT. */

	public void build() {

		setLayout(new BorderLayout(8, 0));
		setPreferredSize(new Dimension(0, 56));
		setOpaque(true);

		sidebarToggle = makeNavButton("☰");
		sidebarToggle.setToolTipText("Show/hide the sidebar");
		sidebarToggle.addActionListener(e -> {
			if (viewStack.getMainWindow() != null)
				viewStack.getMainWindow().toggleSidebar();
		});

		backBtn = makeNavButton("◄");
		backBtn.setDiameter(48);
		backBtn.setPrimary(true);
		forwardBtn = makeNavButton("►");
		forwardBtn.setDiameter(36);
		backBtn.setEnabled(false);
		forwardBtn.setEnabled(false);

		backBtn.addActionListener((ActionEvent e) -> viewStack.back());
		forwardBtn.addActionListener((ActionEvent e) -> viewStack.forward());
	}

	public AppHeader(ViewStack viewStack) {
		this.viewStack = viewStack;
		this.build();
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();
		int w = getWidth(), h = getHeight();

        ((MainWindow)(SwingUtilities.getWindowAncestor(this))).getSkin().paintHeader(this, g2);
		// 1 px white sheen along the very bottom edge
		g2.setColor(HIGHLIGHT);
		g2.drawLine(0, h - 1, w, h - 1);

		g2.dispose();
	}

	protected GlossyButton makeNavButton(String text) {
		GlossyButton btn = new GlossyButton(text);
		btn.setFocusPainted(false);
		btn.setPreferredSize(new Dimension(32, 32));
		btn.setFont(btn.getFont().deriveFont(11f));
		return btn;
	}

	@Override
	public void onNavigate(String uri, boolean canGoBack, boolean canGoForward) {
		backBtn.setEnabled(canGoBack);
		forwardBtn.setEnabled(canGoForward);
	}
}
