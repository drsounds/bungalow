package se.spacify.plugin.search.concept;


import javax.swing.Icon;

import se.spacify.aspect.Aspect;
import se.spacify.aspect.AspectManager;
import se.spacify.concept.Concept;
import se.spacify.concept.ConceptContext;
import se.spacify.navigation.ViewStack;
import se.spacify.plugin.Plugin;
import se.spacify.plugin.search.views.SearchView;

public class SearchConcept implements Concept {
    private Plugin plugin;
    public Plugin getPlugin() {
        return plugin;
    }

    public ViewStack getViewStack() {
        return plugin.getViewStack();
    }

    public SearchConcept(Plugin plugin) {
        this.plugin = plugin;
    }
 
    @Override
    public void onActivate(ConceptContext ctx) {
        ctx.registerView(new SearchView(getViewStack()));
    }

    @Override
    public void onDeactivate() { 
    }
 
    @Override
    public void onRegister(AspectManager<? extends Aspect> aspectManager) {
        // TODO Auto-generated method stub
    }
 
    @Override
    public String getId() {
        // TODO Auto-generated method stub
        return "playlist";
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return "Playlist";
    }

    @Override
    public Icon getIcon() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getIcon'");
    }

}
