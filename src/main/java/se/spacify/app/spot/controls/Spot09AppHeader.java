package se.spacify.app.spot.controls;

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
        getSwingComponent().setLayout(new BoxLayout(getSwingComponent(), BoxLayout.LINE_AXIS));
        getSwingComponent().setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

		backBtn = makeNavButton("◄");
		backBtn.setDiameter(48);
		backBtn.setPrimary(true);
		backBtn.getSwingComponent().setEnabled(false);
		add(backBtn);
		forwardBtn = makeNavButton("►");
		forwardBtn.setDiameter(36);
		forwardBtn.getSwingComponent().setEnabled(false);
		add(forwardBtn);

		backBtn.getSwingComponent().addActionListener((ActionEvent e) -> viewStack.back());
		forwardBtn.getSwingComponent().addActionListener((ActionEvent e) -> viewStack.forward());
		uriField = new TextField("spacify:home");
		uriField.getSwingComponent().setFont(uriField.getSwingComponent().getFont().deriveFont(12f));
		uriField.getSwingComponent().setPreferredSize(new Dimension(260, 28));
		uriField.getSwingComponent().addActionListener(e -> viewStack.navigate(uriField.getSwingComponent().getText().trim()));

		searchField = new TextField();
		searchField.getSwingComponent().putClientProperty("JTextField.placeholderText", "Search...");
		searchField.getSwingComponent().setPreferredSize(new Dimension(180, 28));
		searchField.getSwingComponent().addActionListener(e -> {
			String q = searchField.getSwingComponent().getText().trim();
			if (!q.isEmpty()) {
				String encoded = URLEncoder.encode(q, StandardCharsets.UTF_8);
				viewStack.navigate("spacify:search?q=" + encoded);
			}
		});
        add(searchField);

		center = new Panel(new BorderLayout());
		center.getSwingComponent().setOpaque(false);
		add(center);

		uriField.setVisible(false);

    }
	@Override
	public void onNavigate(String uri, boolean canGoBack, boolean canGoForward) {
		backBtn.getSwingComponent().setEnabled(canGoBack);
		forwardBtn.getSwingComponent().setEnabled(canGoForward);
		if (uri != null)
			uriField.getSwingComponent().setText(uri);
	}
    
}
