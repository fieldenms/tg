package ua.com.fielden.platform.eql.stage2.elements;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static ua.com.fielden.platform.eql.stage2.elements.PathsToTreeTransformator.transform;
import static ua.com.fielden.platform.types.tuples.T2.t2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import ua.com.fielden.platform.eql.meta.AbstractPropInfo;
import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage2.elements.operands.EntProp2;
import ua.com.fielden.platform.eql.stage2.elements.sources.Child;
import ua.com.fielden.platform.eql.stage2.elements.sources.ChildGroup;
import ua.com.fielden.platform.eql.stage2.elements.sources.IQrySource2;
import ua.com.fielden.platform.eql.stage2.elements.sources.QrySource2BasedOnPersistentType;
import ua.com.fielden.platform.eql.stage3.elements.Table;
import ua.com.fielden.platform.eql.stage3.elements.sources.IQrySource3;
import ua.com.fielden.platform.types.tuples.T2;

public class TransformationContext {

    private final Map<String, Table> tables = new HashMap<>();
    private final Map<IQrySource2<?>, List<ChildGroup>> sourceChildren = new HashMap<>();
    private final Map<IQrySource2<?>, Map<String, T2<IQrySource3, Object>>> resolutions = new HashMap<>();

    private TransformationContext() {
    }

    public TransformationContext(final Map<String, Table> tables, final PropsResolutionContext context, final Set<EntProp2> props) {
        this.tables.putAll(tables);
        for (final Entry<IQrySource2<?>, SortedSet<Child>> el : transform(props, context.getDomainInfo()).entrySet()) {
            this.sourceChildren.put(el.getKey(), convertToGroup(el.getValue()));
        }
    }
   
    private List<ChildGroup> convertToGroup(final SortedSet<Child> children) {
        final List<ChildGroup> result = new ArrayList<>();
        final List<AbstractPropInfo<?>> items = new ArrayList<>();
        final Map<AbstractPropInfo<?>, Set<Child>> map = new HashMap<>();
        final Map<AbstractPropInfo<?>, SortedMap<String, QrySource2BasedOnPersistentType>> mapSources = new HashMap<>();
        
        for (final Child child : children) {
            if (items.contains(child.main)) {
                map.get(child.main).add(child);
                if (child.source != null) {
                    mapSources.get(child.main).put(child.source.contextId, child.source);
                }
            } else {
                items.add(child.main);
                final Set<Child> itemChildren = new HashSet<>();
                final SortedMap<String, QrySource2BasedOnPersistentType> itemSources = new TreeMap<>();

                itemChildren.add(child);
                if (child.source != null) {
                    itemSources.put(child.source.contextId, child.source);    
                }
                
                map.put(child.main, itemChildren);
                mapSources.put(child.main, itemSources);
            }
        }
        
        for (final AbstractPropInfo<?> item : items) {
            final Child first = map.get(item).iterator().next();
            final SortedSet<Child> mergedItems = new TreeSet<>();
            
            final Set<T2<String, IQrySource2<?>>> groupPaths = new HashSet<>();
            
            for (final Child c : map.get(item)) {
                mergedItems.addAll(c.items);
                if (c.fullPath != null) {
                    groupPaths.add(t2(c.fullPath, c.parentSource));    
                }
                
            }

            final List<ChildGroup> groupItems = convertToGroup(mergedItems);
            result.add(new ChildGroup(item, groupItems, groupPaths, first.required, mapSources.get(item).isEmpty() ? first.source : mapSources.get(item).entrySet().iterator().next().getValue(), first.expr));
        }   
        
        
        return result;
    }
    
    public Table getTable(final String sourceFullClassName) {
        return tables.get(sourceFullClassName);
    }

    public List<ChildGroup> getSourceChildren(final IQrySource2<?> source) {
        final List<ChildGroup> result = sourceChildren.get(source);
        return result != null ? result : emptyList();
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