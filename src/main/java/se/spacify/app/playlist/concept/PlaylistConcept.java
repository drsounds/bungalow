package se.spacify.app.playlist.concept;


import javax.swing.Icon;

import se.spacify.aspect.Aspect;
import se.spacify.aspect.AspectManager;
import se.spacify.concept.Concept;
import se.spacify.concept.ConceptContext;
import se.spacify.navigation.ViewStack;
import se.spacify.app.Application;
import se.spacify.app.playlist.views.PlaylistView;

public class PlaylistConcept implements Concept {
    private Application plugin;
    public Application getApplication() {
        return plugin;
    }

    public ViewStack getViewStack() {
        return plugin.getViewStack();
    }

    public PlaylistConcept(Application plugin) {
        this.plugin = plugin;
    }
 
    @Override
    public void onActivate(ConceptContext ctx) {
        ctx.registerView(new PlaylistView(getViewStack()));
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
