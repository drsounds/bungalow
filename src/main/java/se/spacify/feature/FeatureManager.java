package se.spacify.feature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import se.spacify.aspect.AspectManager;
import se.spacify.navigation.SPViewStack;
import se.spacify.navigation.SidebarNode;
import se.spacify.service.Feature;

public class FeatureManager implements AspectManager<Feature> {
    private Map<String, Feature> features = new HashMap<String, Feature>();

    public static FeatureManager getInstance() {
        if (instance == null) instance = new FeatureManager();
        return instance;
    }

    private static FeatureManager instance;
    public void register(Feature feature) {
        features.put(feature.getId(), feature);
        feature.onRegister(this);
    }

    /** Drop a feature from the registry (its views/nodes are removed by the caller). */
    public void unregister(Feature feature) {
        features.remove(feature.getId());
    }

    /** Drop a feature from the registry (its views/nodes are removed by the caller). */
    public void unregister(String featureId) {
        features.remove(featureId);
    }

    /**
     * Wire all registered features into the running UI.
     * Call this after MainWindow has been built and the SPViewStack is live.
     *
     * @param viewStack     the app's main view stack
     * @param sidebarRoot   root node of the sidebar JTree model
     */
    public void activateFeatures(SPViewStack viewStack, DefaultMutableTreeNode sidebarRoot) {
        for (Feature f : features.values()) {
            f.getViews().forEach(viewStack::registerView);
            for (SidebarNode sn : f.getSidebarNodes()) {
                sidebarRoot.add(buildTreeNode(sn));
            }
        }
    }

    private static DefaultMutableTreeNode buildTreeNode(SidebarNode sn) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(sn);
        for (SidebarNode child : sn.getChildren()) node.add(buildTreeNode(child));
        return node;
    }

    public Collection<Feature> all() { return Collections.unmodifiableCollection(features.values()); }

    @Override
    public Feature get(String featureId) {
        return features.get(featureId);
    }
}
