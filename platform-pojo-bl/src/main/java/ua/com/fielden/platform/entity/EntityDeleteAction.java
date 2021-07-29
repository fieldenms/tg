package ua.com.fielden.platform.entity;

import static ua.com.fielden.platform.entity.NoKey.NO_KEY;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * Master entity object.
 *
 * @author TG Team
 *
 */
@KeyType(NoKey.class)
@CompanionObject(EntityDeleteActionCo.class)
public class EntityDeleteAction extends AbstractFunctionalEntityWithCentreContext<NoKey> {
    
    @IsProperty
    @Title("Entity Type")
    private Class<? extends AbstractEntity<?>> entityType;
    
    @IsProperty(Long.class)
    @Title("Selected Entity IDs")
    private Set<Long> selectedEntityIds = new HashSet<>();

    protected EntityDeleteAction() {
        setKey(NO_KEY);
    }

    @Observable
    protected EntityDeleteAction setSelectedEntityIds(final Set<Long> selectedEntityIds) {
        this.selectedEntityIds.clear();
        this.selectedEntityIds.addAll(selectedEntityIds);
        return this;
    }

    public Set<Long> getSelectedEntityIds() {
        return Collections.unmodifiableSet(selectedEntityIds);
    }

    @Observable
    public EntityDeleteAction setEntityType(final Class<? extends AbstractEntity<?>> entityType) {
        this.entityType = entityType;
        return this;
    }

    public Class<? extends AbstractEntity<?>> getEntityType() {
        return entityType;
    }
}