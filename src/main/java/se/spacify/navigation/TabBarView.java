package se.spacify.navigation;

import se.spacify.controls.TabbedPane;

public class TabBarView extends View {
    private TabbedPane tabbedPane;

    public TabbedPane getTabbedPane() {
        return tabbedPane;
    }

    public TabBarView(ViewStack viewStack) {
        super(viewStack);        
        this.tabbedPane = new TabbedPane();
        add(this.tabbedPane);
        
    }

    @Override
    public boolean acceptsUri(String uri) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void navigate(String uri) {
        // TODO Auto-generated method stub
    }

  
}
