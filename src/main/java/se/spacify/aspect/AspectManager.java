package se.spacify.aspect;

import java.util.Collection;

public interface AspectManager<T extends Aspect> {
    static <T extends Aspect> AspectManager<T> getInstance() {
        // Implementation would depend on the specific implementation
        return null;
    }
    void register(T aspect);
    void unregister(String aspectId);
    void unregister(T aspect);
    T get(String featureId);
    Collection<T> all();
}
