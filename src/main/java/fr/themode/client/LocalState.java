package fr.themode.client;

import java.util.HashMap;
import java.util.Map;

public class LocalState {

    private Map<String, Object> components;

    public LocalState() {
        this.components = new HashMap<>();
    }

    public void setComponent(String key, Object value) {
        this.components.put(key, value);
    }

    public <T> T getComponent(String key) {
        return (T) components.getOrDefault(key, null);
    }

    protected void update(LocalState localState) {
        this.components = localState.getComponents();
    }

    private Map<String, Object> getComponents() {
        return components;
    }
}
