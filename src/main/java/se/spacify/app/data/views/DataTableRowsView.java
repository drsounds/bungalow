package se.spacify.app.data.views;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import se.spacify.app.data.DataRepository;
import se.spacify.navigation.TabBarView;
import se.spacify.navigation.ViewStack;
import se.spacify.content.*;

public class DataTableRowsView extends TabBarView {

    private DataTableRowsPage overview;
	protected String tableSlug;

    private static final Pattern URI = Pattern.compile("spacify:table:([a-z]+)");

	public DataTableRowsView(ViewStack viewStack, DataRepository repository) {
		super(viewStack);
		// TODO Auto-generated constructor stub
		this.overview = new DataTableRowsPage(this, viewStack, repository);
		getTabbedPane().addTab("overview", "Overview", this.overview);
	}

	@Override public boolean acceptsUri(String uri) { return uri != null && URI.matcher(uri).matches(); }

    @Override
    public void navigate(String uri) {
        Matcher m = URI.matcher(uri);
        tableSlug = m.matches() ? m.group(1) : null;
    }
    
    public void reload() {
    	this.overview.reload();
    }

    @Override public void onShow() { reload(); }

}
