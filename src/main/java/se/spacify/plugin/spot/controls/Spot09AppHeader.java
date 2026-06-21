package se.spacify.plugin.spot.controls;

import java.awt.BorderLayout;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;

import se.spacify.controls.Panel;
import se.spacify.controls.TextField;
import se.spacify.navigation.ViewStack;
import se.spacify.ui.AppHeader;

public class Spot09AppHeader extends AppHeader {
    protected TextField uriField;

	protected TextField searchField;

    private Panel center;
    public Spot09AppHeader(ViewStack viewStack) {
        super(viewStack);
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

		backBtn = makeNavButton("◄");
		backBtn.setDiameter(48);
		backBtn.setPrimary(true);
		backBtn.setEnabled(false);
		add(backBtn);
		forwardBtn = makeNavButton("►");
		forwardBtn.setDiameter(36);
		forwardBtn.setEnabled(false);
		add(forwardBtn);

		backBtn.addActionListener((ActionEvent e) -> viewStack.back());
		forwardBtn.addActionListener((ActionEvent e) -> viewStack.forward());
		uriField = new TextField("spacify:home");
		uriField.setFont(uriField.getFont().deriveFont(12f));
		uriField.setPreferredSize(new Dimension(260, 28));
		uriField.addActionListener(e -> viewStack.navigate(uriField.getText().trim()));

		searchField = new TextField();
		searchField.putClientProperty("JTextField.placeholderText", "Search...");
		searchField.setPreferredSize(new Dimension(180, 28));
		searchField.addActionListener(e -> {
			String q = searchField.getText().trim();
			if (!q.isEmpty()) {
				String encoded = URLEncoder.encode(q, StandardCharsets.UTF_8);
				viewStack.navigate("spacify:search?q=" + encoded);
			}
		});
        add(searchField);

		center = new Panel(new BorderLayout());
		center.setOpaque(false);
		add(center);

		uriField.setVisible(false);

    }
	@Override
	public void onNavigate(String uri, boolean canGoBack, boolean canGoForward) {
		backBtn.setEnabled(canGoBack);
		forwardBtn.setEnabled(canGoForward);
		if (uri != null)
			uriField.setText(uri);
	}
    
}
