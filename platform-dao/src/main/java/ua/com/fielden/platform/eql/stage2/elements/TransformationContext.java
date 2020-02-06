package ua.com.fielden.platform.eql.stage2.elements;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ua.com.fielden.platform.eql.stage2.elements.sources.ChildGroup;
import ua.com.fielden.platform.eql.stage2.elements.sources.IQrySource2;
import ua.com.fielden.platform.eql.stage3.elements.Table;
import ua.com.fielden.platform.eql.stage3.elements.sources.IQrySource3;
import ua.com.fielden.platform.types.tuples.T2;

public class TransformationContext {

    private final Map<String, Table> tables = new HashMap<>();
    private final Map<IQrySource2<?>, List<ChildGroup>> sourceChildren = new HashMap<>();
    private final Map<IQrySource2<?>, Map<String, T2<IQrySource3, Object>>> resolutions = new HashMap<>();
    private final Map<String, Object> paramValues = new HashMap<>();
 
    public TransformationContext(final Map<String, Table> tables, final Map<IQrySource2<?>, List<ChildGroup>> sourceChildren) {
        this.tables.putAll(tables);
        this.sourceChildren.putAll(sourceChildren);
    }
   
    public Table getTable(final String sourceFullClassName) {
        return tables.get(sourceFullClassName);
    }

    public int getNextParamId() {
        return paramValues.size() + 1;
    }
    
    public Map<String, Object> getParamValues() {
        return unmodifiableMap(paramValues);
    }

    public List<ChildGroup> getSourceChildren(final IQrySource2<?> source) {
        final List<ChildGroup> result = sourceChildren.get(source);
        return result != null ? result : emptyList();
    }

    public TransformationContext cloneWithResolutions(final T2<String, IQrySource2<?>> sr1, final T2<IQrySource3, Object> sr2) {
        final TransformationContext result = new TransformationContext(tables, sourceChildren);
        result.resolutions.putAll(resolutions);
        result.paramValues.putAll(paramValues);
        final Map<String, T2<IQrySource3, Object>> existing = result.resolutions.get(sr1._2);
        if (existing != null) {
            existing.put(sr1._1, sr2);
        } else {
            final Map<String, T2<IQrySource3, Object>> created = new HashMap<>();
            created.put(sr1._1, sr2);
            result.resolutions.put(sr1._2, created);
        }

        return result;
    }

    public TransformationContext cloneWithParamValue(final String paramName, final Object paramValue) {
        final TransformationContext result = new TransformationContext(tables, sourceChildren);
        result.resolutions.putAll(resolutions);
        result.paramValues.putAll(paramValues);
        result.paramValues.put(paramName, paramValue);
        return result;
    }
    
    public T2<IQrySource3, Object> resolve(final IQrySource2<?> source, final String path) {
        final T2<IQrySource3, Object> result = resolutions.get(source).get(path);

        if (result == null) {
            System.out.println(format("CAN'T FIND path [%s] in source [%s].", path, source));
        }

        return result;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        for (final Entry<IQrySource2<?>, Map<String, T2<IQrySource3, Object>>> el1 : resolutions.entrySet()) {
            sb.append(" - " + el1.getKey() + ": \n");
            for (final Entry<String, T2<IQrySource3, Object>> el2 : el1.getValue().entrySet()) {
                sb.append("\n               [" + el2.getKey() + "] ==> (" + el2.getValue()._1 + " : " + el2.getValue()._2 + ")");
            }
        }

        return sb.toString();
    }
}