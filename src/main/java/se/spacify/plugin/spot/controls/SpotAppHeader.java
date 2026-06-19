package se.spacify.plugin.spot.controls;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.swing.BoxLayout;

import javax.swing.JTextField;

import se.spacify.controls.Panel;

import se.spacify.navigation.SPViewStack;
import se.spacify.ui.AppHeader;

public class SpotAppHeader extends AppHeader {
    protected JTextField uriField;

	protected JTextField searchField;

    private Panel center;
    public SpotAppHeader(SPViewStack viewStack) {
        super(viewStack);
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

		uriField = new JTextField("spacify:home");
		uriField.setFont(uriField.getFont().deriveFont(12f));
		uriField.setPreferredSize(new Dimension(260, 28));
		uriField.addActionListener(e -> viewStack.navigate(uriField.getText().trim()));
        add(uriField);
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
        add(searchField);

		center = new Panel(new BorderLayout());
		center.setOpaque(false);

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
