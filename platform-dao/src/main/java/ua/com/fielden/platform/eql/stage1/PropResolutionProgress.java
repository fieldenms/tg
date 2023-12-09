package ua.com.fielden.platform.eql.stage1;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.eql.meta.query.AbstractQuerySourceItem;

public class PropResolutionProgress {
    private final List<AbstractQuerySourceItem<?>> resolved = new ArrayList<>();
    private final List<String> pending = new ArrayList<>();

    public PropResolutionProgress(final String pendingAsOneDotNotatedProp) {
        this.pending.addAll(asList(pendingAsOneDotNotatedProp.split("\\.")));
    }

    private PropResolutionProgress(final List<String> pending, final List<AbstractQuerySourceItem<?>> resolved) {
        this.pending.addAll(pending);
        this.resolved.addAll(resolved);
    }

    public PropResolutionProgress registerResolutionAndClone(final AbstractQuerySourceItem<?> propResolutionStep) {
        final List<AbstractQuerySourceItem<?>> updatedResolved = new ArrayList<>(resolved);
        updatedResolved.add(propResolutionStep);
        return new PropResolutionProgress(pending.subList(1, pending.size()), updatedResolved);
    }

    public List<AbstractQuerySourceItem<?>> getResolved() {
        return unmodifiableList(resolved);
    }

    public String getNextPending() {
        return pending.get(0);
    }

    public boolean isSuccessful() {
        return pending.isEmpty() || (lastPendingIsId() && lastResolvedHasPersistentEntityType());
    }

    private boolean lastPendingIsId() {
        return pending.size() == 1 && pending.get(0).equals(ID);
    }

    private boolean lastResolvedHasPersistentEntityType() {
        // by ensuring that part preceding ID is not just entity, but persistent entity it is achieved that implicit calc-prop of ID on an union entity is not skipped here
        return !resolved.isEmpty() && isPersistedEntityType(resolved.get(resolved.size() - 1).javaType());
    }
}