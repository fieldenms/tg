package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

public abstract class ActivatableAbstractEntity<K extends Comparable<K>> extends AbstractEntity<K> {
    private static final long serialVersionUID = 1L;

    public static final String ACTIVE = "active";
    public static final String REF_COUNT = "refCount";

    @IsProperty
    @MapTo("ACTIVE_FLAG_")
    @Title(value = "Active", desc = "Designates whether an entity instance is active or not.")
    private boolean active;

    @IsProperty
    @MapTo
    @Title(value = "Ref Count", desc = "The count of active entities pointing to this entity.")
    private Integer refCount;

    @Observable
    public ActivatableAbstractEntity<K> setRefCount(final Integer refCount) {
        this.refCount = refCount;
        return this;
    }

    public Integer getRefCount() {
        return refCount;
    }

    @Observable
    protected ActivatableAbstractEntity<K> setActive(final boolean active) {
        this.active = active;
        return this;
    }

    public boolean isActive() {
        return active;
    }

}
