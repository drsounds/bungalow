package se.spacify.plugin.music.concept;


import javax.swing.Icon;

import se.spacify.aspect.Aspect;
import se.spacify.aspect.AspectManager;
import se.spacify.concept.Concept;
import se.spacify.concept.ConceptContext;
import se.spacify.navigation.ViewStack;
import se.spacify.plugin.Plugin;

public class MusicConcept implements Concept {
    private Plugin plugin;
    public Plugin getPlugin() {
        return plugin;
    }

    public ViewStack getViewStack() {
        return plugin.getViewStack();
    }

    public MusicConcept(Plugin plugin) {
        this.plugin = plugin;
    }
 
    @Override
    public void onActivate(ConceptContext ctx) {
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
        return "music";
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return "Music";
    }

    @Override
    public Icon getIcon() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getIcon'");
    }

}
