package se.spacify.app.ai.concept;

import javax.swing.Icon;

import se.spacify.app.Application;
import se.spacify.aspect.Aspect;
import se.spacify.aspect.AspectManager;
import se.spacify.concept.Concept;
import se.spacify.concept.ConceptContext;

public class LLMConcept implements Concept {

    @Override
    public String getId() {
        // TODO Auto-generated method stub
        return "LLM";
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return "LLM Concept";
    }

    @Override
    public void onRegister(AspectManager<? extends Aspect> aspectManager) {
        // TODO Auto-generated method stub 
    }

    @Override
    public void onActivate(ConceptContext ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onActivate'");
    }

    @Override
    public void onDeactivate() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onDeactivate'");
    }

    @Override
    public Icon getIcon() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getIcon'");
    }

    @Override
    public Application getApplication() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getApplication'");
    }
    
}
