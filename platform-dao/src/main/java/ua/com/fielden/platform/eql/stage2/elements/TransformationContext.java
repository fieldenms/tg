package ua.com.fielden.platform.eql.stage2.elements;

import static java.util.Collections.emptySet;
import static ua.com.fielden.platform.eql.stage2.elements.PathsToTreeTransformator.transform;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage2.elements.sources.Child;
import ua.com.fielden.platform.eql.stage2.elements.sources.IQrySource2;
import ua.com.fielden.platform.eql.stage3.elements.Table;
import ua.com.fielden.platform.eql.stage3.elements.sources.IQrySource3;
import ua.com.fielden.platform.types.tuples.T2;

public class TransformationContext {
    
    private final Map<String, Table> tables = new HashMap<>();
    private final Map<IQrySource2<?>, SortedSet<Child>> sourceChildren = new HashMap<>();
    private final Map<IQrySource2<?>, Map<String, T2<IQrySource3, String>>> resolutions = new HashMap<>();

    public TransformationContext(final Map<String, Table> tables, final PropsResolutionContext context) {
        this.tables.putAll(tables);
        this.sourceChildren.putAll(transform(context.getResolvedProps(), context.getDomainInfo()));
    }
    
    private TransformationContext() {
    }
    
    public Table getTable(final String sourceFullClassName) {
        return tables.get(sourceFullClassName);
    }
    
    public Set<Child> getSourceChildren(final IQrySource2<?> source) {
        final  Set<Child> result = sourceChildren.get(source);
        return result != null ?  result : emptySet();
    }
    
    public TransformationContext cloneWithResolutions(final IQrySource2<?> source, final Map<String, T2<IQrySource3, String>> sourceResolutions) {
        final TransformationContext result = new TransformationContext();
        result.tables.putAll(tables);
        result.sourceChildren.putAll(sourceChildren);
        result.resolutions.putAll(resolutions);
        result.resolutions.put(source, sourceResolutions);
        return result;
    }
    
    public T2<IQrySource3, String> resolve(final IQrySource2<?> source, final String path) {
        return resolutions.get(source).get(path);
    }
}