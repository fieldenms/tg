package ua.com.fielden.platform.eql.meta;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

public class ResolutionContext {
    public final List<AbstractPropInfo<?, ?>> resolved = new ArrayList<>();
    public final List<String> pending;

    public ResolutionContext(final List<String> pending) {
        this.pending = pending;
    }
    
    public ResolutionContext(final String pendingAsOneDotNotatedProp) {
        this.pending = asList(pendingAsOneDotNotatedProp.split("\\."));
    }

    public ResolutionContext registerResolutionAndClone(final AbstractPropInfo<?, ?> propResolutionStep) {
        final ResolutionContext result = new ResolutionContext(pending.subList(1, pending.size()));
        result.resolved.addAll(resolved);
        result.resolved.add(propResolutionStep);
        return result;
    }
}