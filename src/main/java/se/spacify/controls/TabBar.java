package se.spacify.controls;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;

public class TabBar extends Panel {

	private final List<TabBarButton> tabButtons = new ArrayList<TabBarButton>();
	private final Map<String, TabBarButton> tabs = new HashMap<String, TabBarButton>();
	
	public TabBar(Control<?> parent) {
		super(parent);
		getComponent().setLayout(new BoxLayout(getComponent(), BoxLayout.LINE_AXIS));
	}

	public List<TabBarButton> getTabButtons() {
		return tabButtons;
	}
	
	public void addTab(String id, String name) {
		TabBarButton button = new TabBarButton(this, id, name);
		tabs.put(id, button);
		button.addChangeListener(new TabButton.ChangeListener() { 
			@Override
			public void onToggleChanged(int state) {
				// TODO Auto-generated method stub
			   if(state == TabButton.PRESSED){
			        System.out.println("button is selected");
			   } else if(state == TabButton.DEFAULT) {
				   System.out.println("button is not selected");
			   }
			}
		});
		add(button);
	}
	public void removeTab(TabButton button) {
		remove(button);
	}
}
