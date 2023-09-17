package ua.com.fielden.platform.eql.stage1.sources;

import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.eql.meta.PropType;

public class YieldInfoNode {
    public final String name;
    public final PropType propType;
    public final boolean nonnullable;
    private final Map<String, YieldInfoNode> items = new HashMap<>();

    public YieldInfoNode(final String name, final PropType propType, final boolean nonnullable, final Map<String, YieldInfoNode> items) {
        this.name = name;
        this.propType = propType;
        this.items.putAll(items);
        this.nonnullable = nonnullable;
    }
}