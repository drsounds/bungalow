package se.spacify.app.ai.service;

import se.spacify.aspect.Aspect;
import se.spacify.aspect.AspectManager;
import se.spacify.service.Service;

public class LargeLanguageModelService implements Service {

    /**
     * TODO Implement the logic which instructs an LLM to create a new app based on the prompt based on the docs/plugin-system
     * This service could be used by the CreateController to handle the POST request and create a new app based on the prompt.
     * And different LLMService implementations could be used to support different LLMs.
     */

    @Override
    public void onRegister(AspectManager<? extends Aspect> aspectManager) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onRegister'");
    }

    @Override
    public String getId() {
        // TODO Auto-generated method stub
        return "LLM";
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return "LLM Service";
    }
    
}
