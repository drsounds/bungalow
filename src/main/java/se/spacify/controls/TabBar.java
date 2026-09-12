package se.spacify.controls;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;

public class TabBar extends Panel {

	private final List<TabBarButton> tabButtons = new ArrayList<TabBarButton>();
	private final Map<String, TabBarButton> tabs = new HashMap<String, TabBarButton>();
	
	
	private List<SelectedTabChangedListener> selectedTabChangedListeners = new ArrayList<>();
	public TabBar(Control<?> parent) {
		super(parent);
		getComponent().setLayout(new BoxLayout(getComponent(), BoxLayout.LINE_AXIS));
		getComponent().setBorder(BorderFactory.createEmptyBorder());
		setSize(Integer.MAX_VALUE, 28);
		
	}

	public List<TabBarButton> getTabButtons() {
		return tabButtons;
	}
	
	public void setSelectedTabById(String id) {
		for (TabBarButton b : tabButtons) {
			b.setSelected(false);
		}
		TabBarButton tabBarButton = tabs.get(id);
		if (tabBarButton != null) {
			tabBarButton.setSelected(true);;
		}
	}
	
	public void addTab(String id, String name) {
		TabBarButton button = new TabBarButton(this, id, name);
		tabs.put(id, button);
		button.addChangeListener(new ToggleButton.ChangeListener() {
			@Override
			public void onToggleChanged(int state) {
				// TODO Auto-generated method stub

			}
		});
		button.addChangeListener(new TabButton.ChangeListener() { 
			@Override
			public void onToggleChanged(int state) {
				// TODO Auto-generated method stub
				   if(state == TabButton.PRESSED){
				        // System.out.println("button is selected");
				        for (SelectedTabChangedListener l : selectedTabChangedListeners) {
				        	l.onSelectedTabChanged(new SelectedTabChangedEventArgs(id, true));
				        }
				   } else if(state == TabButton.DEFAULT) {
					   // System.out.println("button is not selected");
				   }

			}
		});
		add(button);
	}

	@Override
	public void paintSurface(Graphics g) {
		Graphics2D g2 = (Graphics2D)g.create();
		getSkin().paintTabBar(this, g2);
		g2.dispose();
		super.paintSurface(g2);
	}

	public void removeTab(TabButton button) {
		remove(button);
	}
	public class SelectedTabChangedEventArgs {
		private String tabId;
		private boolean selected;
		public String getTabId() {
			return tabId;
		}
		public SelectedTabChangedEventArgs(String tabId, boolean selected) {
			this.tabId = tabId;
			this.selected = selected;
		}
		public boolean isSelected() {
			return selected;
		}
		public void setSelected(boolean selected) {
			this.selected = selected;
		}
	}
	public interface SelectedTabChangedListener {
		public void onSelectedTabChanged(SelectedTabChangedEventArgs e);
	}

	public void addSelectedTabChangedListener(SelectedTabChangedListener value) {
		// TODO Auto-generated method stub
		selectedTabChangedListeners.add(value);
	}
}
