package ua.com.fielden.platform.eql.stage2.elements;

import static java.lang.String.format;
import static java.util.Collections.emptySet;
import static ua.com.fielden.platform.eql.stage2.elements.PathsToTreeTransformator.transform;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
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
    private final Map<IQrySource2<?>, Map<String, T2<IQrySource3, Object>>> resolutions = new HashMap<>();

    public TransformationContext(final Map<String, Table> tables, final PropsResolutionContext context) {
        this.tables.putAll(tables);
        this.sourceChildren.putAll(transform(context.getResolvedProps(), context.getDomainInfo()));
        System.out.println("-======================================");
        for (final Entry<IQrySource2<?>, SortedSet<Child>> el : sourceChildren.entrySet()) {
            System.out.println("\n source: " + el.getKey() + "\n");
            for (final Child c : el.getValue()) {
                System.out.println("++++++++++++++++");
                System.out.println(c);
                if (!c.dependencies.isEmpty()) {
                    System.out.println(" +deps:");
                    for (final Child d : c.dependencies) {
                        System.out.println(d);
                    }
                    
                }
                if (!c.items.isEmpty()) {
                    System.out.println(" +children:");
                    for (final Child c1 : c.items) {
                        System.out.println(c1);
                        if (!c1.dependencies.isEmpty()) {
                            System.out.println(" ++deps:");
                            for (final Child d : c1.dependencies) {
                                System.out.println(d);
                            }
                            
                        }
                        
                        
                        
                        
                        
                        
                    }
                    
                }
            }
        }
    }

    private TransformationContext() {
    }

    public Table getTable(final String sourceFullClassName) {
        return tables.get(sourceFullClassName);
    }

    public Set<Child> getSourceChildren(final IQrySource2<?> source) {
        final Set<Child> result = sourceChildren.get(source);
        return result != null ? result : emptySet();
    }

    public TransformationContext cloneWithResolutions(final T2<String, IQrySource2<?>> sr1, final T2<IQrySource3, Object> sr2) {
        final TransformationContext result = new TransformationContext();
        result.tables.putAll(tables);
        result.sourceChildren.putAll(sourceChildren);
        result.resolutions.putAll(resolutions);
        Map<String, T2<IQrySource3, Object>> existing = result.resolutions.get(sr1._2);
        if (existing == null) {
            existing = new HashMap<>();
            existing.put(sr1._1, sr2);
            result.resolutions.put(sr1._2, existing);
        } else {
            final Map<String, T2<IQrySource3, Object>> merged = new HashMap<>();
            merged.putAll(existing);
            merged.put(sr1._1, sr2);
            result.resolutions.put(sr1._2, merged);
        }

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