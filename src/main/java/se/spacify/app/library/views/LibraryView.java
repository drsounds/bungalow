package se.spacify.app.library.views;

import se.spacify.navigation.TabBarView;
import se.spacify.navigation.ViewStack;

public class LibraryView extends TabBarView {
 

    public LibraryView(ViewStack viewStack) {
        super(viewStack); 
        getTabbedPane().addTab("overview", "Library", new LibraryPage(this, viewStack));
    }
 

    @Override public boolean acceptsUri(String uri) { return uri != null && uri.matches("spacify:library.*"); }
    @Override public void navigate(String uri) {}
    @Override public String getName() { return "Your Library"; }
}
