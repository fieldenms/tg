package ua.com.fielden.platform.eql.stage1.elements.sources;

import java.util.HashMap;
import java.util.Map;

public class YieldInfoNode {
    public final String name;
    public final Class<?> javaType;
    public final Object hibType;
    private final Map<String, YieldInfoNode> items = new HashMap<>();
    
    public YieldInfoNode(final String name, final Class<?> javaType, final Object hibType, final Map<String, YieldInfoNode> items) {
        this.name = name;
        this.javaType = javaType;
        this.hibType = hibType;
        this.items.putAll(items);
    }
}