package se.spacify.app.claude.agent;

import se.spacify.aspect.Aspect;
import se.spacify.aspect.AspectManager;
import se.spacify.llm.Agent;

public class ClaudeAgent extends Agent {

    /**
     * TODO Integrate Claude Managed Agent API
     */
    @Override
    public String getId() {
        // TODO Auto-generated method stub
        return "claude";
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return "Claude";
    }

    @Override
    public void onRegister(AspectManager<? extends Aspect> aspectManager) {
        // TODO Auto-generated method stub
       
    }
    
}
