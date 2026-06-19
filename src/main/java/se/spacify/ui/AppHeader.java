package se.spacify.ui;

import se.spacify.controls.GlassPanel;
import se.spacify.controls.GlossyButton;
import se.spacify.controls.TabButton;
import se.spacify.navigation.NavigationListener;
import se.spacify.navigation.SPViewStack;
import se.spacify.ui.theme.ThemeManager;
import se.spacify.web.FaviconFetcher;
import se.spacify.web.SiteUri;
import se.spacify.web.StoreCatalog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class AppHeader extends JPanel implements NavigationListener {

	private static final Color CHROME_DARK = new Color(14, 14, 14);
	private static final Color HIGHLIGHT = new Color(255, 255, 255, 35);

	private final SPViewStack viewStack;
	private final GlossyButton backBtn;
	private final GlossyButton forwardBtn;
	private final JTextField uriField;
	private final JTextField searchField;
	private TabButton nowPlayingTab;
	private TabButton libraryTab;
	private JButton storesBtn;
	/** Cached store favicons, fetched off the EDT. */
	private final Map<String, Icon> faviconCache = new HashMap<>();
	private TabButton mediaGuideTab;

	public AppHeader(SPViewStack viewStack) {
		this.viewStack = viewStack;
		setLayout(new BorderLayout(8, 0));
		setPreferredSize(new Dimension(0, 56));
		setOpaque(true);

		JButton sidebarToggle = makeNavButton("☰");
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

		JPanel navButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
		navButtons.setOpaque(false);
		// navButtons.add(sidebarToggle);
		navButtons.add(backBtn);
		navButtons.add(forwardBtn);
		navButtons.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

		uriField = new JTextField("spacify:home");
		uriField.setFont(uriField.getFont().deriveFont(12f));
		uriField.setPreferredSize(new Dimension(260, 28));
		uriField.addActionListener(e -> viewStack.navigate(uriField.getText().trim()));

		searchField = new JTextField();
		searchField.putClientProperty("JTextField.placeholderText", "Search...");
		searchField.setPreferredSize(new Dimension(180, 28));
		searchField.addActionListener(e -> {
			String q = searchField.getText().trim();
			if (!q.isEmpty()) {
				String encoded = URLEncoder.encode(q, StandardCharsets.UTF_8);
				viewStack.navigate("spacify:search?q=" + encoded);
			}
		});

		JPanel center = new JPanel(new BorderLayout());
		center.setOpaque(false);
		navButtons.add(uriField);
		uriField.setVisible(false);

		// WMP-style tab strip, flush with the bottom edge of the nav bar.
		nowPlayingTab = new TabButton("Now Playing");
		nowPlayingTab.addActionListener(e -> {
			MainWindow mw = viewStack.getMainWindow();
			if (mw != null) {
				mw.setSidebarVisible(false);
				mw.navigate("spacify:now-playing");
			}
		});
		libraryTab = new TabButton("Library");
		libraryTab.addActionListener(e -> {
			MainWindow mw = viewStack.getMainWindow();
			if (mw != null) {
				mw.setSidebarVisible(true);
				mw.navigate("spacify:library");
			}
		});
		mediaGuideTab = new TabButton("Media Guide");
		mediaGuideTab.addActionListener(e -> {
			MainWindow mw = viewStack.getMainWindow();
			if (mw != null) {
				mw.setSidebarVisible(true);
				mw.navigate("spacify:store:www.last.fm");
			}
		});
		JPanel tabBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
		tabBar.setOpaque(false);
		tabBar.add(nowPlayingTab);
		tabBar.add(libraryTab);
		tabBar.add(mediaGuideTab);
		center.add(tabBar, BorderLayout.SOUTH);
		JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
		right.setOpaque(false);

		// right.add(searchField);
		GlassPanel storePanel = new GlassPanel();
		storePanel.setPreferredSize(new Dimension(300, 46));
		storePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		storePanel.setLeadingDiagonal(true); // sharp left edge, bottom longer than top
		storePanel.setDiagonalInset(65);
		storesBtn = makeNavButton("Stores ▾");
		storesBtn.setPreferredSize(new Dimension(160, 32));
		storesBtn.setToolTipText("Open a music Service");
		// Transparent & borderless so it floats on the glass field.
		storesBtn.setOpaque(false);
		storesBtn.setContentAreaFilled(false);
		storesBtn.setBorderPainted(false);
		storesBtn.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
		storesBtn.setHorizontalAlignment(SwingConstants.LEFT);
		storesBtn.addActionListener(e -> buildStoresMenu().show(storesBtn, 0, storesBtn.getHeight()));
		storePanel.add(storesBtn);

		add(navButtons, BorderLayout.WEST);
		add(center, BorderLayout.CENTER);
		add(right, BorderLayout.EAST);

		right.add(storePanel, BorderLayout.CENTER);

		loadFavicons();

		viewStack.addNavigationListener(this);
		ThemeManager.addChangeListener(this::repaint);
	}

	/** Build the stores popup from the catalogue, using cached favicons. */
	private JPopupMenu buildStoresMenu() {
		JPopupMenu menu = new JPopupMenu();
		for (StoreCatalog.Store store : StoreCatalog.STORES) {
			JMenuItem item = new JMenuItem(store.name(), faviconCache.get(store.host()));
			item.addActionListener(e -> {
				if (viewStack.getMainWindow() != null)
					viewStack.getMainWindow().navigate(store.uri());
				else
					viewStack.navigate(store.uri());
			});
			menu.add(item);
		}
		return menu;
	}

	/** Fetch each store's favicon off the EDT and cache it for the popup. */
	private void loadFavicons() {
		new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() {
				for (StoreCatalog.Store store : StoreCatalog.STORES) {
					byte[] png = FaviconFetcher.fetch(store.host());
					if (png != null) {
						Icon icon = new ImageIcon(png);
						SwingUtilities.invokeLater(() -> {
							faviconCache.put(store.host(), icon);
							updateStoresButton(viewStack.getCurrentUri());
						});
					}
				}
				return null;
			}
		}.execute();
	}

	/**
	 * While on a store, show its favicon + name in the dropdown; else "Stores ▾".
	 */
	private void updateStoresButton(String uri) {
		if (storesBtn == null)
			return;
		if (uri != null && uri.startsWith(SiteUri.STORE_PREFIX)) {
			String host = SiteUri.host(uri, SiteUri.STORE_PREFIX);
			StoreCatalog.Store store = StoreCatalog.STORES.stream().filter(s -> s.host().equals(host)).findFirst()
					.orElse(null);
			storesBtn.setText((store != null ? store.name() : host) + " ▾");
			storesBtn.setIcon(host != null ? faviconCache.get(host) : null);
		} else {
			storesBtn.setText("Stores ▾");
			storesBtn.setIcon(null);
		}
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

	private GlossyButton makeNavButton(String text) {
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
		if (uri != null)
			uriField.setText(uri);
		updateStoresButton(uri);
		nowPlayingTab.setSelected(uri != null && uri.startsWith("spacify:now-playing"));
		libraryTab.setSelected(uri != null && uri.startsWith("spacify:library"));
	}
}
