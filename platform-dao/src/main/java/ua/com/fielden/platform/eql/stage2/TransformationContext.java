package ua.com.fielden.platform.eql.stage2;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ua.com.fielden.platform.eql.exceptions.EqlStage3ProcessingException;
import ua.com.fielden.platform.eql.meta.DomainMetadataUtils;
import ua.com.fielden.platform.eql.stage2.sources.ChildGroup;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;
import ua.com.fielden.platform.eql.stage3.Table;
import ua.com.fielden.platform.eql.stage3.sources.ISource3;
import ua.com.fielden.platform.types.tuples.T2;

public class TransformationContext {

    private final TablesAndSourceChildren tablesAndSourceChildren;
    private final Map<String, Map<String, T2<ISource3, Object>>> resolutions = new HashMap<>();
    private final Map<String, Object> paramValues = new HashMap<>();
    public final int sqlId;

    public TransformationContext(final TablesAndSourceChildren tablesAndSourceChildren) {
        this(tablesAndSourceChildren, emptyMap(), emptyMap(), 0);
    }

    private TransformationContext(final TablesAndSourceChildren tablesAndSourceChildren, final Map<String, Map<String, T2<ISource3, Object>>> resolutions, final Map<String, Object> paramValues, final int sqlId) {
        this.tablesAndSourceChildren = tablesAndSourceChildren;
        this.resolutions.putAll(resolutions);
        this.paramValues.putAll(paramValues);
        this.sqlId = sqlId;
    }

    public Table getTable(final String sourceFullClassName) {
        return tablesAndSourceChildren.getTables().get(DomainMetadataUtils.getOriginalEntityTypeFullName(sourceFullClassName));
    }

    public int getNextParamId() {
        return paramValues.size() + 1;
    }

    public Map<String, Object> getParamValues() {
        return unmodifiableMap(paramValues);
    }

    public List<ChildGroup> getSourceChildren(final ISource2<?> source) {
        final List<ChildGroup> result = tablesAndSourceChildren.getSourceChildren().get(source.id());
        return result != null ? result : emptyList();
    }

    public TransformationContext cloneWithNextSqlId() {
        return new TransformationContext(tablesAndSourceChildren, resolutions, paramValues, sqlId + 1);
    }

    public TransformationContext cloneWithResolutions(final T2<String, String> sr1, final T2<ISource3, Object> sr2) {
        final TransformationContext result = new TransformationContext(tablesAndSourceChildren, resolutions, paramValues, sqlId);

        final Map<String, T2<ISource3, Object>> existing = result.resolutions.get(sr1._2);
        if (existing != null) {
            existing.put(sr1._1, sr2);
        } else {
            final Map<String, T2<ISource3, Object>> created = new HashMap<>();
            created.put(sr1._1, sr2);
            result.resolutions.put(sr1._2, created);
        }

        return result;
    }

    public TransformationContext cloneWithParamValue(final String paramName, final Object paramValue) {
        final TransformationContext result = new TransformationContext(tablesAndSourceChildren, resolutions, paramValues, sqlId);
        result.paramValues.put(paramName, paramValue);
        return result;
    }

    public T2<ISource3, Object> resolve(final ISource2<?> source, final String path) {

        final Map<String, T2<ISource3, Object>> sourceMap = resolutions.get(source.id());
        if (sourceMap == null) {
            throw new EqlStage3ProcessingException(format("Can't find sourceMap for path [%s] in source [%s].", path, source));
        }

        final T2<ISource3, Object> result = sourceMap.get(path);

        if (result == null) {
            throw new EqlStage3ProcessingException(format("Can't find path [%s] in source [%s].", path, source));
        }

        return result;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        for (final Entry<String, Map<String, T2<ISource3, Object>>> el1 : resolutions.entrySet()) {
            sb.append(" - Source with id [" + el1.getKey() + "]: \n");
            for (final Entry<String, T2<ISource3, Object>> el2 : el1.getValue().entrySet()) {
                sb.append("\n               [" + el2.getKey() + "] ==> (" + el2.getValue()._1 + " : " + el2.getValue()._2 + ")");
            }
        }

        return sb.toString();
    }
}