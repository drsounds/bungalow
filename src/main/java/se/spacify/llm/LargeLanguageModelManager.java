package se.spacify.llm;

import java.util.Collection;
import java.util.Collections;

import se.spacify.aspect.BaseAspectManager;

import se.spacify.ui.MainWindow;

public class LargeLanguageModelManager extends BaseAspectManager<LargeLanguageModel> {
    
    public LargeLanguageModelManager(MainWindow mainWindow) {
        super(mainWindow);
    }
 
    public void register(LargeLanguageModel feature) {
        super.register(feature);
        feature.onRegister(this);
    }

    /** Drop a feature from the registry (its views/nodes are removed by the caller). */
    public void unregister(LargeLanguageModel feature) {
        super.unregister(feature);
    }

    /** Drop a feature from the registry (its views/nodes are removed by the caller). */
    public void unregister(String featureId) {
        super.unregister(featureId);
    }
 
    public Collection<LargeLanguageModel> all() { return Collections.unmodifiableCollection(getNodes().values()); }

    @Override
    public LargeLanguageModel get(String featureId) {
        return getNodes().get(featureId);
    }
}
