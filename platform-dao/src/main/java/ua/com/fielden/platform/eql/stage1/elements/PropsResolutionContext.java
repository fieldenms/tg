package ua.com.fielden.platform.eql.stage1.elements;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.meta.AbstractPropInfo;
import ua.com.fielden.platform.eql.meta.EntityInfo;
import ua.com.fielden.platform.eql.stage2.elements.operands.EntProp2;
import ua.com.fielden.platform.eql.stage2.elements.operands.EntValue2;
import ua.com.fielden.platform.eql.stage2.elements.sources.IQrySource2;
import ua.com.fielden.platform.eql.stage3.elements.sources.IQrySource3;

public class PropsResolutionContext {
    private final List<List<IQrySource2<? extends IQrySource3>>> sources;
    private final Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> domainInfo;
    private final Map<IQrySource2<?>, Map<String, List<AbstractPropInfo<?>>>> resolvedProps;

    public PropsResolutionContext(final Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> domainInfo) {
        this.domainInfo = new HashMap<>(domainInfo);
        this.sources = buildSourcesStackForNewQuery(emptyList());
        this.resolvedProps = new HashMap<>();
    }
    
    public PropsResolutionContext(final Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> domainInfo, final List<List<IQrySource2<? extends IQrySource3>>> sources, final Map<IQrySource2<?>, Map<String, List<AbstractPropInfo<?>>>> props) {
        this.domainInfo = new HashMap<>(domainInfo);
        this.sources = sources;
        this.resolvedProps = new HashMap<>(props);
    }

    public PropsResolutionContext produceForCorrelatedSubquery() {
        return new PropsResolutionContext(domainInfo, buildSourcesStackForNewQuery(sources), resolvedProps);
    }

    public PropsResolutionContext produceForUncorrelatedSubquery() {
        return new PropsResolutionContext(domainInfo, buildSourcesStackForNewQuery(emptyList()), resolvedProps);
    }
    
    private static List<List<IQrySource2<? extends IQrySource3>>> buildSourcesStackForNewQuery(final List<List<IQrySource2<? extends IQrySource3>>> existingSources) {
        final List<List<IQrySource2<? extends IQrySource3>>> srcs = new ArrayList<>();
        srcs.add(new ArrayList<>());
        srcs.addAll(existingSources);
        return srcs;
    }
    
    public PropsResolutionContext cloneWithAdded(final IQrySource2<? extends IQrySource3> transformedSource, final Map<IQrySource2<?>, Map<String, List<AbstractPropInfo<?>>>> resolvedProps) {
        final List<List<IQrySource2<? extends IQrySource3>>> srcs = new ArrayList<>();
        srcs.addAll(sources);
        srcs.get(0).add(transformedSource); // adding source to current query list of sources
        return new PropsResolutionContext(domainInfo, srcs, resolvedProps);
    }

    public PropsResolutionContext cloneWithAdded(final EntProp2 transformedProp) {
        final List<List<IQrySource2<? extends IQrySource3>>> srcs = new ArrayList<>();
        srcs.addAll(sources);
        final Map<IQrySource2<?>, Map<String, List<AbstractPropInfo<?>>>> props = new HashMap<>(resolvedProps);
        
        final Map<String, List<AbstractPropInfo<?>>> existing = props.get(transformedProp.source);
        
        if (existing == null) {
            final Map<String, List<AbstractPropInfo<?>>> propMap = new HashMap<>();
            propMap.put(transformedProp.name, transformedProp.getPath());
            props.put(transformedProp.source, propMap);
        } else {
            if (!existing.containsKey(transformedProp.name)) {
                existing.put(transformedProp.name, transformedProp.getPath());    
            }
        };
        
        return new PropsResolutionContext(domainInfo, srcs, props);
    }

    public PropsResolutionContext cloneWithAdded(final EntValue2 value) {
        final List<List<IQrySource2<? extends IQrySource3>>> srcs = new ArrayList<>();
        srcs.addAll(sources);
        return new PropsResolutionContext(domainInfo, srcs, resolvedProps);
    }

    public PropsResolutionContext cloneNew() {
        final List<List<IQrySource2<? extends IQrySource3>>> srcs = new ArrayList<>();
        srcs.addAll(sources);
        return new PropsResolutionContext(domainInfo, srcs, resolvedProps);
    }

    public Map<IQrySource2<?>, Map<String, List<AbstractPropInfo<?>>>> getResolvedProps() {
        return unmodifiableMap(resolvedProps);
    }

    public List<List<IQrySource2<? extends IQrySource3>>> getSources() {
        return unmodifiableList(sources);
    }

    public Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> getDomainInfo() {
        return unmodifiableMap(domainInfo);
    }
}