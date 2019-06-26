package ua.com.fielden.platform.eql.stage2.elements;

import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.eql.stage2.elements.sources.IQrySource2;
import ua.com.fielden.platform.eql.stage3.elements.Table;
import ua.com.fielden.platform.eql.stage3.elements.sources.IQrySource3;

public class TransformationContext {
    
    private final Map<String, Table> tables = new HashMap<>();
    private final Map<IQrySource2<?>, IQrySource3> sources = new HashMap<>();

    public TransformationContext(final Map<String, Table> tables) {
        this.tables.putAll(tables);
    }
    
    private TransformationContext(final Map<String, Table> tables, final Map<IQrySource2<?>, IQrySource3> sources) {
        this(tables);
        this.sources.putAll(sources);
    }
    
    public Table getTable(final String sourceFullClassName) {
        return tables.get(sourceFullClassName);
    }

    public IQrySource3 getSource(final IQrySource2<?> source) {
        return sources.get(source);
    }
    
    public TransformationContext cloneWithAdded(final IQrySource3 transformedSource, final IQrySource2<?> originalSource) {
        final Map<IQrySource2<?>, IQrySource3> newSources = new HashMap<>();
        newSources.putAll(sources);
        newSources.put(originalSource, transformedSource);
        return new TransformationContext(tables, newSources);
    }
}