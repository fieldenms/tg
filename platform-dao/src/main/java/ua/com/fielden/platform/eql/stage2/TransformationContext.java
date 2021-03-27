package ua.com.fielden.platform.eql.stage2;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static ua.com.fielden.platform.eql.meta.DomainMetadataUtils.getOriginalEntityTypeFullName;
import static ua.com.fielden.platform.types.tuples.T2.t2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ua.com.fielden.platform.eql.stage2.sources.ChildGroup;
import ua.com.fielden.platform.eql.stage3.Table;
import ua.com.fielden.platform.eql.stage3.sources.ISource3;
import ua.com.fielden.platform.types.tuples.T2;

public class TransformationContext {

    private final TablesAndSourceChildren tablesAndSourceChildren;
    private final Map<String, Map<String, T2<ISource3, Object>>> resolutions = new HashMap<>();
    private final Map<String, Object> paramValues = new HashMap<>();
    public final int sqlId;
    public final int paramId; //incremented after usage as part of the `cloneWithParamValue(..)` method

    public TransformationContext(final TablesAndSourceChildren tablesAndSourceChildren) {
        this(tablesAndSourceChildren, emptyMap(), emptyMap(), 0, 1);
    }

    private TransformationContext(final TablesAndSourceChildren tablesAndSourceChildren, final Map<String, Map<String, T2<ISource3, Object>>> resolutions, final Map<String, Object> paramValues, final int sqlId, final int paramId) {
        this.tablesAndSourceChildren = tablesAndSourceChildren;
        this.resolutions.putAll(resolutions);
        this.paramValues.putAll(paramValues);
        this.sqlId = sqlId;
        this.paramId = paramId;
    }

    public Table getTable(final String sourceFullClassName) {
        return tablesAndSourceChildren.getTables().get(getOriginalEntityTypeFullName(sourceFullClassName));
    }

    public Map<String, Object> getParamValues() {
        return unmodifiableMap(paramValues);
    }

    public List<ChildGroup> getSourceChildren(final String sourceId) {
        final List<ChildGroup> result = tablesAndSourceChildren.getSourceChildren().get(sourceId);
        return result != null ? result : emptyList();
    }

    public TransformationContext cloneWithNextSqlId() {
        return new TransformationContext(tablesAndSourceChildren, resolutions, paramValues, sqlId + 1, paramId);
    }

    public TransformationContext cloneWithResolutions(final ISource3 source, final List<ChildGroup> children) {
        final TransformationContext result = new TransformationContext(tablesAndSourceChildren, resolutions, paramValues, sqlId, paramId);
        
        for (final ChildGroup fc : children) {
            for (final Entry<String, String> el : fc.paths().entrySet()) {
                final Map<String, T2<ISource3, Object>> existing = result.resolutions.get(el.getValue());
                if (existing != null) {
                    existing.put(el.getKey(), t2(source, fc.expr == null ? fc.name : fc.expr));
                } else {
                    final Map<String, T2<ISource3, Object>> created = new HashMap<>();
                    created.put(el.getKey(), t2(source, fc.expr == null ? fc.name : fc.expr));
                    result.resolutions.put(el.getValue(), created);
                }
            }
        }
        return result;
    }

    public TransformationContext cloneWithParamValue(final String paramName, final Object paramValue) {
        final TransformationContext result = new TransformationContext(tablesAndSourceChildren, resolutions, paramValues, sqlId, paramId + 1);
        result.paramValues.put(paramName, paramValue);
        return result;
    }

    public T2<ISource3, Object> resolve(final String sourceId, final String path) {
        return resolutions.get(sourceId).get(path);
    }
}