package ua.com.fielden.platform.eql.stage1;

import com.google.common.collect.ImmutableList;
import ua.com.fielden.platform.eql.meta.query.AbstractQuerySourceItem;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.splitPropPath;

public class PropResolutionProgress {
    private final List<AbstractQuerySourceItem<?>> resolved;
    private final List<String> pending;

    public PropResolutionProgress(final String pendingAsOneDotNotatedProp) {
        this.pending = splitPropPath(pendingAsOneDotNotatedProp);
        this.resolved = ImmutableList.of();
    }

    private PropResolutionProgress(final List<String> pending, final List<AbstractQuerySourceItem<?>> resolved) {
        this.pending = pending;
        this.resolved = resolved;
    }

    public PropResolutionProgress registerResolutionAndClone(final AbstractQuerySourceItem<?> propResolutionStep) {
        final var updatedResolved = ImmutableList. <AbstractQuerySourceItem<?>> builderWithExpectedSize(resolved.size() + 1)
                .addAll(resolved)
                .add(propResolutionStep)
                .build();
        return new PropResolutionProgress(pending.subList(1, pending.size()), updatedResolved);
    }

    public List<AbstractQuerySourceItem<?>> getResolved() {
        return resolved;
    }

    public String getNextPending() {
        return pending.getFirst();
    }

    public boolean isSuccessful() {
        return pending.isEmpty() || (lastPendingIsId() && lastResolvedHasPersistentEntityType());
    }

    private boolean lastPendingIsId() {
        return pending.size() == 1 && pending.getFirst().equals(ID);
    }

    private boolean lastResolvedHasPersistentEntityType() {
        // by ensuring that part preceding ID is not just entity, but persistent entity it is achieved that implicit calc-prop of ID on an union entity is not skipped here
        return !resolved.isEmpty() && isPersistedEntityType(resolved.getLast().javaType());
    }
}
