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

	public void build() {

		setLayout(new BorderLayout(8, 0));
		setPreferredSize(new Dimension(0, 56));
		setOpaque(true);

		sidebarToggle = makeNavButton("☰");
		sidebarToggle.getComponent().setToolTipText("Show/hide the sidebar");
		sidebarToggle.getComponent().addActionListener(e -> {
			if (viewStack.getMainWindow() != null)
				viewStack.getMainWindow().toggleSidebar();
		});

		backBtn = makeNavButton("◄");
		backBtn.setDiameter(48);
		backBtn.setPrimary(true);
		forwardBtn = makeNavButton("►");
		forwardBtn.setDiameter(36);
		backBtn.getComponent().setEnabled(false);
		forwardBtn.getComponent().setEnabled(false);

		backBtn.getComponent().addActionListener((ActionEvent e) -> viewStack.back());
		forwardBtn.getComponent().addActionListener((ActionEvent e) -> viewStack.forward());
	}

	public AppHeader(ViewStack viewStack) {
		this.viewStack = viewStack;
		this.build();
	}

	@Override
	protected void paintSurface(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();
		int w = getWidth(), h = getHeight();

		getSkin().paintHeader(getComponent(), g2);
		// 1 px white sheen along the very bottom edge
		g2.setColor(HIGHLIGHT);
		g2.drawLine(0, h - 1, w, h - 1);

		g2.dispose();
	}

	protected GlossyButton makeNavButton(String text) {
		GlossyButton btn = new GlossyButton(text);
		btn.getComponent().setFocusPainted(false);
		btn.getComponent().setPreferredSize(new Dimension(32, 32));
		btn.getComponent().setFont(btn.getComponent().getFont().deriveFont(11f));
		return btn;
	}

	@Override
	public void onNavigate(String uri, boolean canGoBack, boolean canGoForward) {
		backBtn.getComponent().setEnabled(canGoBack);
		forwardBtn.getComponent().setEnabled(canGoForward);
	}
}
