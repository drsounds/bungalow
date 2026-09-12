package se.spacify.controls;

import java.awt.BorderLayout;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JTabbedPane;
import se.spacify.navigation.ViewStack;

/**
 * A tabbed container control wrapping a {@link JTabbedPane}. Reach the widget
 * through {@link #getComponent()}; child controls are added (with a tab title) via
 * {@link #addTab(String, Control)}.
 */
public class TabbedPane extends Panel { 
	private TabBar tabBar;

	private Panel stack;
	
	private Map<String, TabBarButton> tabButtons = new HashMap<String, TabBarButton>();
	
	public TabbedPane(Control<?> parent, ViewStack viewStack) {
		super(parent, viewStack);
		getComponent().setLayout(new BorderLayout());

		tabBar = new TabBar(this);
		add(tabBar, BorderLayout.NORTH);

		stack = new Panel(this);
		add(stack, BorderLayout.CENTER);
		
		
	}

	public TabbedPane() {
		super();
		getComponent().setLayout(new BorderLayout());

		tabBar = new TabBar(this);
		add(tabBar, BorderLayout.NORTH);

		stack = new Panel(this);
		add(stack, BorderLayout.CENTER);
		
		
	}

	/** Add a child control as a titled tab (mounts {@code child.getComponent()}). */
	public TabbedPane addTab(String id, String title, Control<?> child) {
		TabBarButton tabButton = new TabBarButton(this, id, title);
		tabButtons.put(id, tabButton);
		return this;
	}
}
