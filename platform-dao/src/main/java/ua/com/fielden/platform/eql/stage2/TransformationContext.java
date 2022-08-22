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
    private final Map<Integer, Map<String, T2<ISource3, Object>>> resolutions = new HashMap<>(); // TODO consider replacing Object with 2 concrete types  
    private final Map<String, Object> paramValuesByNames = new HashMap<>();
    private final Map<Object, String> paramNamesByValues = new HashMap<>();
    public final int sqlId;
    private final int paramId; //incremented after each new param name generation

    public TransformationContext(final TablesAndSourceChildren tablesAndSourceChildren) {
        this(tablesAndSourceChildren, emptyMap(), emptyMap(), emptyMap(), 0, 1);
    }

    private TransformationContext(final TablesAndSourceChildren tablesAndSourceChildren, 
            final Map<Integer, Map<String, T2<ISource3, Object>>> resolutions,
            final Map<String, Object> paramValuesByNames,
            final Map<Object, String> paramNamesByValues,
            final int sqlId, final int paramId) {
        this.tablesAndSourceChildren = tablesAndSourceChildren;
        this.resolutions.putAll(resolutions);
        this.paramValuesByNames.putAll(paramValuesByNames);
        this.paramNamesByValues.putAll(paramNamesByValues);
        this.sqlId = sqlId;
        this.paramId = paramId;
    }

    public Table getTable(final String sourceFullClassName) {
        return tablesAndSourceChildren.getTables().get(getOriginalEntityTypeFullName(sourceFullClassName));
    }

    public Map<String, Object> getParamValues() {
        return unmodifiableMap(paramValuesByNames);
    }
    
    public T2<String, TransformationContext> obtainParamNameAndUpdateContext(final Object paramValue) {
        final String existingParamName = paramNamesByValues.get(paramValue);
        if (existingParamName != null) {
            return t2(existingParamName, this);
        } else {
            final String paramName = "P_" + paramId;
            final TransformationContext result = new TransformationContext(tablesAndSourceChildren, resolutions, paramValuesByNames, paramNamesByValues, sqlId, paramId + 1);
            result.paramValuesByNames.put(paramName, paramValue);
            result.paramNamesByValues.put(paramValue, paramName);

            return t2(paramName, result);
        }
    }

    public List<ChildGroup> getSourceChildren(final Integer sourceId) {
        final List<ChildGroup> result = tablesAndSourceChildren.getSourceChildren().get(sourceId);
        return result != null ? result : emptyList();
    }

    public TransformationContext cloneWithNextSqlId() {
        return new TransformationContext(tablesAndSourceChildren, resolutions, paramValuesByNames, paramNamesByValues, sqlId + 1, paramId);
    }

    public TransformationContext cloneWithResolutions(final ISource3 source, final List<ChildGroup> children) {
        final TransformationContext result = new TransformationContext(tablesAndSourceChildren, resolutions, paramValuesByNames, paramNamesByValues, sqlId, paramId);
        
        for (final ChildGroup fc : children) {
            for (final Entry<String, Integer> el : fc.paths().entrySet()) {
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

    public T2<ISource3, Object> resolve(final Integer sourceId, final String path) {
    	return resolutions.get(sourceId).get(path);
    }
}