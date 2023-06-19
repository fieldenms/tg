package ua.com.fielden.platform.eql.stage1.sources;

import java.util.HashMap;
import java.util.Map;

public class YieldInfoNode {
    public final String name;
    public final Class<?> javaType;
    public final boolean required;
    private final Map<String, YieldInfoNode> items = new HashMap<>();

    public YieldInfoNode(final String name, final Class<?> javaType, final boolean required, final Map<String, YieldInfoNode> items) {
        this.name = name;
        this.javaType = javaType;
        this.items.putAll(items);
        this.required = required;
    }
}