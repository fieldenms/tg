package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

public abstract class ActivatableAbstractEntity<K extends Comparable<K>> extends AbstractEntity<K> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @MapTo("ACTIVE_FLAG_")
    @Title(value = "Active", desc = "Designates whether an entity instance is active or not.")
    private boolean active;


    @Observable
    protected ActivatableAbstractEntity<K> setActive(final boolean active) {
        this.active = active;
        return this;
    }

    public boolean isActive() {
        return active;
    }




}
