package se.spacify.app.llm;

import java.util.Collection;
import java.util.Collections;

import se.spacify.aspect.BaseAspectManager;

import se.spacify.ui.MainWindow;

public class LLMManager extends BaseAspectManager<LLM> {
    
    public LLMManager(MainWindow mainWindow) {
        super(mainWindow);
    }
 
    public void register(LLM feature) {
        super.register(feature);
        feature.onRegister(this);
    }

    /** Drop a feature from the registry (its views/nodes are removed by the caller). */
    public void unregister(LLM feature) {
        super.unregister(feature);
    }

    /** Drop a feature from the registry (its views/nodes are removed by the caller). */
    public void unregister(String featureId) {
        super.unregister(featureId);
    }
 
    public Collection<LLM> all() { return Collections.unmodifiableCollection(getNodes().values()); }

    @Override
    public LLM get(String featureId) {
        return getNodes().get(featureId);
    }
}
