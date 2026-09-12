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
	
	private Map<String, TabBarButton> tabBarButtons = new HashMap<String, TabBarButton>();
	private Map<String, Control<?>> controls = new HashMap<String, Control<?>>();
 
	public void setActiveTabById(String id) {
		TabBarButton tabBarButton = tabBarButtons.get(id);
		tabBarButton.setSelected(true);
	}
	public TabbedPane(Control<?> parent, ViewStack viewStack) {
		super(parent, viewStack);
		init();
	}
	
	private void init() {
		getComponent().setLayout(new BorderLayout());

		tabBar = new TabBar(this);
		add(tabBar, BorderLayout.NORTH);

		stack = new Panel(this);
		add(stack, BorderLayout.CENTER);
		
		tabBar.addSelectedTabChangedListener(new TabBar.SelectedTabChangedListener () {
			public void onSelectedTabChanged(TabBar.SelectedTabChangedEventArgs e) {
				for (Control<?> c : controls.values()) {
					c.hide();
				}
				Control<?> control = controls.get(e.getTabId());
				if (control != null) {
					control.show();
				}
			}
		});
		tabBar.setSelectedTabById("overview");
	}

	public TabbedPane() {
		super();
		init();
	}

	/** Add a child control as a titled tab (mounts {@code child.getComponent()}). */
	public TabbedPane addTab(String id, String title, Control<?> child) {
		TabBarButton tabButton = new TabBarButton(this, id, title);
		tabBarButtons.put(id, tabButton);
		tabBar.addTab(id, title);
		controls.put(id, child);
		stack.add(child, BorderLayout.CENTER);
		return this;
	}
}
