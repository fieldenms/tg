package ua.com.fielden.platform.eql.meta;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;

import java.util.ArrayList;
import java.util.List;

public class ResolutionContext {
    private final List<AbstractPropInfo<?>> resolved = new ArrayList<>();
    private final List<String> pending = new ArrayList<>();

    public ResolutionContext(final String pendingAsOneDotNotatedProp) {
        this.pending.addAll(asList(pendingAsOneDotNotatedProp.split("\\.")));
    }

    private ResolutionContext(final List<String> pending, final List<AbstractPropInfo<?>> resolved) {
        this.pending.addAll(pending);
        this.resolved.addAll(resolved);
    }

    public ResolutionContext registerResolutionAndClone(final AbstractPropInfo<?> propResolutionStep) {
        final List<AbstractPropInfo<?>> updatedResolved = new ArrayList<>(); 
        updatedResolved.addAll(resolved);
        updatedResolved.add(propResolutionStep);
        return new ResolutionContext(pending.subList(1, pending.size()), updatedResolved);
    }
    
    public boolean isSuccessful() {
        return pending.isEmpty() || (pending.size() == 1 && pending.get(0).equals(ID) && !resolved.isEmpty() && isEntityType(resolved.get(resolved.size() - 1).javaType()));
    }
    
    public List<AbstractPropInfo<?>> getResolved() {
        return unmodifiableList(resolved);
    }
    
    public String getNextPending() {
        return pending.get(0);
    }
}