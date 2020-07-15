package ua.com.fielden.platform.eql.meta;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;

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
        final List<AbstractPropInfo<?>> updatedResolved = new ArrayList<>(resolved); 
        final List<String> updatedPending = pending.subList(1, pending.size());  
        if ((updatedPending.size() == 1 && updatedPending.get(0).equals(ID) && /*!updatedResolved.isEmpty() && */isPersistedEntityType(propResolutionStep.javaType()))) {
            updatedResolved.add(new PrimTypePropInfo<>(propResolutionStep.name, propResolutionStep.hibType, Long.class, propResolutionStep.expression));
            return new ResolutionContext(emptyList(), updatedResolved);
        } else {
            updatedResolved.add(propResolutionStep);
            return new ResolutionContext(updatedPending, updatedResolved);
        }
    }
    
    public boolean isSuccessful() {
        return pending.isEmpty();// || (pending.size() == 1 && pending.get(0).equals(ID) && !resolved.isEmpty() && isPersistedEntityType(resolved.get(resolved.size() - 1).javaType()));
    }
    
    public List<AbstractPropInfo<?>> getResolved() {
        return unmodifiableList(resolved);
    }
    
    public String getNextPending() {
        return pending.get(0);
    }
}