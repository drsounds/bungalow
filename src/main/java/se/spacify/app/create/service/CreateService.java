package se.spacify.app.create.service;

import se.spacify.aspect.Aspect;
import se.spacify.aspect.AspectManager;
import se.spacify.service.Service;

public class CreateService implements Service {

    @Override
    public void onRegister(AspectManager<? extends Aspect> aspectManager) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onRegister'");
    }

    public void createApp(String prompt) {
        /*
        TODO Implement the logic which instructs an LLM create a new app based on the prompt based on the docs/plugin-system
        
        */
    }

    @Override
    public String getId() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getId'");
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getName'");
    }
    
}
