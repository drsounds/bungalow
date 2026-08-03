package se.spacify.app.create.view;

import se.spacify.app.spider.views.SpiderTabBarView;

import se.spacify.navigation.ViewStack;

public class CreateView extends SpiderTabBarView {

    public CreateView(ViewStack viewStack) {
        super(viewStack, "spacify:create");
        //TODO Auto-generated constructor stub
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return "Create";
    }

    @Override
    public boolean acceptsUri(String uri) {
        // TODO Auto-generated method stub
       
        return uri != null && uri.startsWith("spacify:create");
    }

    @Override
    public void navigate(String uri) {
        // TODO Auto-generated method stub
        super.navigate(uri);
    }
    
}
