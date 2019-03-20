package ua.com.fielden.platform.test.domain.entities;

import ua.com.fielden.platform.entity.AbstractEntity;

public abstract class RotableLocation<T extends Comparable<T>> extends AbstractEntity<T> {

    protected RotableLocation() {
    }

    public RotableLocation(final Long id, final T key, final String desc) {
        super(id, key, desc);
    }
}
