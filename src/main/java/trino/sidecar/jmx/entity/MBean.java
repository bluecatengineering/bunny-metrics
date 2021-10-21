package trino.sidecar.jmx.entity;

import java.util.Map;

public class MBean {
    private final String name;
    private final Map<String, String> properties;

    public MBean(String name, Map<String, String> properties) {
        this.name = name;
        this.properties = properties;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public String p(String property) {
        return properties.getOrDefault(property, null);
    }
}
