package ua.com.fielden.platform.eql.meta;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage2.elements.EntProp2;
import ua.com.fielden.platform.eql.stage2.elements.EntValue2;
import ua.com.fielden.platform.eql.stage2.elements.IQrySource2;

public class PropsResolutionContext {
    private final List<List<IQrySource2>> sources;
    private final Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> domainInfo;
    private final Set<EntProp2> resolvedProps;

    public PropsResolutionContext(final Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> domainInfo) {
        this.domainInfo = new HashMap<>(domainInfo);
        this.sources = buildSourcesStackForNewQuery(emptyList());
        this.resolvedProps = new HashSet<>();
    }
    
    public PropsResolutionContext(final Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> domainInfo, final List<List<IQrySource2>> sources, final Set<EntProp2> props) {
        this.domainInfo = new HashMap<>(domainInfo);
        this.sources = sources;
        this.resolvedProps = new HashSet<>(props);
    }

    public PropsResolutionContext produceForCorrelatedSubquery() {
        return new PropsResolutionContext(domainInfo, buildSourcesStackForNewQuery(sources), resolvedProps);
    }

    public PropsResolutionContext produceForUncorrelatedSubquery() {
        return new PropsResolutionContext(domainInfo, buildSourcesStackForNewQuery(emptyList()), resolvedProps);
    }
    
    private static List<List<IQrySource2>> buildSourcesStackForNewQuery(final List<List<IQrySource2>> existingSources) {
        final List<List<IQrySource2>> srcs = new ArrayList<>();
        srcs.add(new ArrayList<>());
        srcs.addAll(existingSources);
        return srcs;
    }
    
    public PropsResolutionContext cloneWithAdded(final IQrySource2 transformedSource, final Set<EntProp2> resolvedProps) {
        final List<List<IQrySource2>> srcs = new ArrayList<>();
        srcs.addAll(sources);
        srcs.get(0).add(transformedSource); // adding source to current query list of sources
        return new PropsResolutionContext(domainInfo, srcs, resolvedProps);
    }

    public PropsResolutionContext cloneWithAdded(final EntProp2 transformedProp) {
        final List<List<IQrySource2>> srcs = new ArrayList<>();
        srcs.addAll(sources);
        final Set<EntProp2> props = new HashSet<>(resolvedProps);
        props.add(transformedProp);
        return new PropsResolutionContext(domainInfo, srcs, props);
    }

    public PropsResolutionContext cloneWithAdded(final EntValue2 value) {
        final List<List<IQrySource2>> srcs = new ArrayList<>();
        srcs.addAll(sources);
        return new PropsResolutionContext(domainInfo, srcs, resolvedProps);
    }

    public PropsResolutionContext cloneNew() {
        final List<List<IQrySource2>> srcs = new ArrayList<>();
        srcs.addAll(sources);
        return new PropsResolutionContext(domainInfo, srcs, resolvedProps);
    }

    public Set<EntProp2> getResolvedProps() {
        return unmodifiableSet(resolvedProps);
    }

    public List<List<IQrySource2>> getSources() {
        return unmodifiableList(sources);
    }

    public Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> getDomainInfo() {
        return unmodifiableMap(domainInfo);
    }
}