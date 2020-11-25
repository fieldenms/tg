package ua.com.fielden.platform.eql.stage2;

import static java.util.Collections.unmodifiableMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.eql.stage2.sources.ChildGroup;
import ua.com.fielden.platform.eql.stage3.Table;

public class TablesAndSourceChildren {
    private final Map<String, Table> tables = new HashMap<>();
    private final Map<String, List<ChildGroup>> sourceChildren = new HashMap<>();
    
    public TablesAndSourceChildren(final Map<String, Table> tables, final Map<String, List<ChildGroup>> sourceChildren) {
        this.tables.putAll(tables);
        this.sourceChildren.putAll(sourceChildren);
    }

    public Map<String, Table> getTables() {
        return unmodifiableMap(tables);
    }

    public Map<String, List<ChildGroup>> getSourceChildren() {
        return unmodifiableMap(sourceChildren);
    }
}