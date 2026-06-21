package se.spacify.navigation;

import se.spacify.controls.TabbedPane;

public abstract class TabBarView extends View {
    private TabbedPane tabbedPane;

    public TabbedPane getTabbedPane() {
        return tabbedPane;
    }

    public TabBarView(ViewStack viewStack) {
        super(viewStack);        
        this.tabbedPane = new TabbedPane();

    }

  
}
