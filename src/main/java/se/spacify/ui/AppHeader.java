package se.spacify.ui;

import se.spacify.controls.GlossyButton;

import se.spacify.navigation.NavigationListener;
import se.spacify.navigation.ViewStack;

import se.spacify.controls.Panel;
import se.spacify.controls.Button;

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

		getSwingComponent().setLayout(new BorderLayout(8, 0));
		getSwingComponent().setPreferredSize(new Dimension(0, 56));
		getSwingComponent().setOpaque(true);

		sidebarToggle = makeNavButton("☰");
		sidebarToggle.getSwingComponent().setToolTipText("Show/hide the sidebar");
		sidebarToggle.getSwingComponent().addActionListener(e -> {
			if (viewStack.getMainWindow() != null)
				viewStack.getMainWindow().toggleSidebar();
		});

		backBtn = makeNavButton("◄");
		backBtn.setDiameter(48);
		backBtn.setPrimary(true);
		forwardBtn = makeNavButton("►");
		forwardBtn.setDiameter(36);
		backBtn.getSwingComponent().setEnabled(false);
		forwardBtn.getSwingComponent().setEnabled(false);

		backBtn.getSwingComponent().addActionListener((ActionEvent e) -> viewStack.back());
		forwardBtn.getSwingComponent().addActionListener((ActionEvent e) -> viewStack.forward());
	}

	public AppHeader(ViewStack viewStack) {
		this.viewStack = viewStack;
		this.build();
	}

	@Override
	protected void paintSurface(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();
		int w = getSwingComponent().getWidth(), h = getSwingComponent().getHeight();

		getSkin().paintHeader(this, g2);
		// 1 px white sheen along the very bottom edge
		g2.setColor(HIGHLIGHT);
		g2.drawLine(0, h - 1, w, h - 1);

		g2.dispose();
	}

	protected GlossyButton makeNavButton(String text) {
		GlossyButton btn = new GlossyButton(text);
		btn.getSwingComponent().setFocusPainted(false);
		btn.getSwingComponent().setPreferredSize(new Dimension(32, 32));
		btn.getSwingComponent().setFont(btn.getSwingComponent().getFont().deriveFont(11f));
		return btn;
	}

	@Override
	public void onNavigate(String uri, boolean canGoBack, boolean canGoForward) {
		backBtn.getSwingComponent().setEnabled(canGoBack);
		forwardBtn.getSwingComponent().setEnabled(canGoForward);
	}
}
