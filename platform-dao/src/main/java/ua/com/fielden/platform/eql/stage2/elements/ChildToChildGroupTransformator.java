package ua.com.fielden.platform.eql.stage2.elements;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ua.com.fielden.platform.eql.stage2.elements.operands.Expression2;
import ua.com.fielden.platform.eql.stage2.elements.sources.Child;
import ua.com.fielden.platform.eql.stage2.elements.sources.ChildGroup;
import ua.com.fielden.platform.eql.stage2.elements.sources.QrySource2BasedOnPersistentType;

public class ChildToChildGroupTransformator {
    
    public static List<ChildGroup> convertToGroup(final List<Child> children) {
        if (children.isEmpty()) {
            return emptyList();
        }
        
        // collecting data from children of the same name
        final Map<String, DataForChildGroup> dataByChildName = collectDataFrom(children);
        
        // ordering children for correct dependencies resolution
        final List<String> orderedChildrenNames = orderItems(dataByChildName.values());
        
        // transforming into ChildGroup list
        final List<ChildGroup> result = new ArrayList<>();
        for (final String childName : orderedChildrenNames) {
            final DataForChildGroup dataForChildGroup = dataByChildName.get(childName);
            final List<ChildGroup> groupItems = convertToGroup(dataForChildGroup.items);
            result.add(new ChildGroup(childName, groupItems, dataForChildGroup.paths, dataForChildGroup.required, dataForChildGroup.source, dataForChildGroup.expr));
        }
        
        return result;
    }

    private static Map<String, DataForChildGroup> collectDataFrom(final List<Child> children) {
        final Map<String, DataForChildGroup> dataMap = new HashMap<>();
        
        for (final Child child : children) {
            DataForChildGroup existing = dataMap.get(child.name);
            if (existing == null) {
                final DataForChildGroup added = new DataForChildGroup(child.name, child.required, child.expr, child.dependencies);
                dataMap.put(added.mainName, added);
                existing = added;
            }
            
            if (child.source != null && existing.source == null) {
                existing.source = child.source;
            }
            
            if (child.fullPath != null) {
                existing.paths.put(child.fullPath, child.explicitSourceId);    
            }
            
            existing.dependencies.addAll(child.dependencies);
            
            existing.items.addAll(child.getItems());
        }
        return dataMap;
    }
    
    private static List<String> orderItems(Collection<DataForChildGroup> data) {
        final Map<String, Set<String>> mapOfDependencies = new HashMap<>();
        
        for (final DataForChildGroup item : data) {
            mapOfDependencies.put(item.mainName, new HashSet<>(item.dependencies));
        }

        final List<String> orderedItems = new ArrayList<>();
        
        while (!mapOfDependencies.isEmpty()) {
            String found = null;
            for (Entry<String, Set<String>> el : mapOfDependencies.entrySet()) {
                if (el.getValue().isEmpty()) {
                    found = el.getKey();
                    break;
                }
            }

            orderedItems.add(found);
            mapOfDependencies.remove(found);
            
            for (Entry<String, Set<String>> el : mapOfDependencies.entrySet()) {
                el.getValue().remove(found);
            }
        }
        
        return orderedItems;
    }
    
    private static class DataForChildGroup {
        private final String mainName;
        private QrySource2BasedOnPersistentType source;
        private final boolean required;
        
        private final List<Child> items = new ArrayList<>();
        
        private final Map<String, String> paths = new HashMap<>();
        
        private final Set<String> dependencies = new HashSet<>();

        private final Expression2 expr;
        

        public DataForChildGroup(String mainName, boolean required, Expression2 expr, final Set<String> dependencies) {
            this.mainName = mainName;
            this.required = required;
            this.expr = expr;
            this.dependencies.addAll(dependencies);
        }
    }
}