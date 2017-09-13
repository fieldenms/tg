package ua.com.fielden.platform.entity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * Master entity object.
 *
 * @author Developers
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(IEntityDeleteAction.class)
public class EntityDeleteAction extends AbstractFunctionalEntityWithCentreContext<String> {
    
    @IsProperty
    @Title("Entity Type")
    private Class<?> entityType;
    
    @IsProperty(Long.class)
    @Title("Selected Entity IDs")
    private Set<Long> selectedEntityIds = new HashSet<>();

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
    public EntityDeleteAction setEntityType(final Class<?> entityType) {
        this.entityType = entityType;
        return this;
    }

    public Class<?> getEntityType() {
        return entityType;
    }
}